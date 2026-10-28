package com.example.GestionDeCine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.GestionDeCine.dto.CompraRequestDTO;
import com.example.GestionDeCine.dto.TicketCompraResponseDTO;
import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.model.Asiento;
import com.example.GestionDeCine.model.Entrada;
import com.example.GestionDeCine.model.EstadoCompra;
import com.example.GestionDeCine.model.EstadoEntrada;
import com.example.GestionDeCine.model.Funcion;
import com.example.GestionDeCine.model.Pelicula;
import com.example.GestionDeCine.model.Rol;
import com.example.GestionDeCine.model.Sala;
import com.example.GestionDeCine.model.TicketCompra;
import com.example.GestionDeCine.model.Usuario;
import com.example.GestionDeCine.repository.AsientoRepository;
import com.example.GestionDeCine.repository.EntradaRepository;
import com.example.GestionDeCine.repository.FuncionRepository;
import com.example.GestionDeCine.repository.MembresiaRepository;
import com.example.GestionDeCine.repository.PeliculaRepository;
import com.example.GestionDeCine.repository.TicketCompraRepository;
import com.example.GestionDeCine.repository.UsuarioRepository;

/**
 * Tests unitarios de la compra y cancelacion de entradas. Los repositorios son
 * mocks (Mockito): no se usa la base, solo se prueba la logica del service.
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock private TicketCompraRepository ticketCompraRepository;
    @Mock private MembresiaRepository membresiaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private FuncionRepository funcionRepository;
    @Mock private AsientoRepository asientoRepository;
    @Mock private EntradaRepository entradaRepository;
    @Mock private PeliculaRepository peliculaRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Usuario cliente;
    private Sala sala;
    private Funcion funcion;
    private Asiento a1;
    private Asiento a2;

    @BeforeEach
    void setUp() {
        cliente = new Usuario();
        cliente.setId(1);
        cliente.setRol(Rol.CLIENTE);

        sala = sala(1, "Sala 1");

        Pelicula pelicula = new Pelicula();
        pelicula.setId(1);
        pelicula.setNombre("Interstellar");
        pelicula.setDuracion(169);

        funcion = new Funcion();
        funcion.setId(10);
        funcion.setSala(sala);
        funcion.setPelicula(pelicula);
        funcion.setPrecio(new BigDecimal("4500.00"));
        funcion.setFechaFuncion(LocalDate.now().plusDays(1));
        funcion.setHoraFuncion(LocalTime.of(18, 0));

        a1 = asiento(100, "A", 1, sala);
        a2 = asiento(101, "A", 2, sala);
    }

    @Test
    void comprar_dosButacasLibres_calculaElTotalEnElServidor() {
        stubUsuarioYFuncion();
        when(entradaRepository.findAsientoIdsByFuncionIdAndEstado(10, EstadoEntrada.OCUPADA)).thenReturn(List.of());
        when(asientoRepository.findById(100)).thenReturn(Optional.of(a1));
        when(asientoRepository.findById(101)).thenReturn(Optional.of(a2));
        when(ticketCompraRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TicketCompraResponseDTO ticket = clienteService.comprarEntradas(compra(100, 101));

        assertEquals(new BigDecimal("9000.00"), ticket.getTotal());
        assertEquals(EstadoCompra.CONFIRMADA, ticket.getEstado());
        assertEquals(2, ticket.getEntradas().size());
        assertEquals(EstadoEntrada.OCUPADA, ticket.getEntradas().get(0).getEstado());
    }

    @Test
    void comprar_butacaOcupada_lanzaBusinessExceptionYNoGuardaNada() {
        stubUsuarioYFuncion();
        when(entradaRepository.findAsientoIdsByFuncionIdAndEstado(10, EstadoEntrada.OCUPADA)).thenReturn(List.of(100));
        when(asientoRepository.findById(100)).thenReturn(Optional.of(a1));

        assertThrows(BusinessException.class, () -> clienteService.comprarEntradas(compra(100)));
        verify(ticketCompraRepository, never()).save(any());
    }

    @Test
    void comprar_butacaDeOtraSala_lanzaBusinessException() {
        stubUsuarioYFuncion();
        when(entradaRepository.findAsientoIdsByFuncionIdAndEstado(10, EstadoEntrada.OCUPADA)).thenReturn(List.of());
        when(asientoRepository.findById(200)).thenReturn(Optional.of(asiento(200, "A", 1, sala(2, "Sala 2"))));

        assertThrows(BusinessException.class, () -> clienteService.comprarEntradas(compra(200)));
    }

    @Test
    void comprar_mismaButacaDosVeces_lanzaBusinessException() {
        stubUsuarioYFuncion();

        assertThrows(BusinessException.class, () -> clienteService.comprarEntradas(compra(100, 100)));
    }

    @Test
    void comprar_funcionQueYaEmpezo_lanzaBusinessException() {
        funcion.setFechaFuncion(LocalDate.now().minusDays(1));
        stubUsuarioYFuncion();

        assertThrows(BusinessException.class, () -> clienteService.comprarEntradas(compra(100)));
    }

    @Test
    void comprar_usuarioInexistente_lanzaResourceNotFound() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clienteService.comprarEntradas(compra(100)));
    }

    @Test
    void comprar_usuarioQueNoEsCliente_lanzaBusinessException() {
        cliente.setRol(Rol.ADMINISTRADOR);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(cliente));

        assertThrows(BusinessException.class, () -> clienteService.comprarEntradas(compra(100)));
    }

    @Test
    void cancelar_compraConfirmada_cancelaElTicketYLiberaLasButacas() {
        TicketCompra ticket = ticketConfirmado();
        when(ticketCompraRepository.findById(50)).thenReturn(Optional.of(ticket));

        TicketCompraResponseDTO resultado = clienteService.cancelarCompra(50);

        assertEquals(EstadoCompra.CANCELADA, resultado.getEstado());
        assertEquals(EstadoEntrada.CANCELADA, ticket.getEntradas().get(0).getEstado());
    }

    @Test
    void cancelar_compraYaCancelada_lanzaBusinessException() {
        TicketCompra ticket = ticketConfirmado();
        ticket.setEstado(EstadoCompra.CANCELADA);
        when(ticketCompraRepository.findById(50)).thenReturn(Optional.of(ticket));

        assertThrows(BusinessException.class, () -> clienteService.cancelarCompra(50));
    }

    @Test
    void cancelar_funcionQueYaEmpezo_lanzaBusinessException() {
        TicketCompra ticket = ticketConfirmado();
        funcion.setFechaFuncion(LocalDate.now().minusDays(1));
        when(ticketCompraRepository.findById(50)).thenReturn(Optional.of(ticket));

        assertThrows(BusinessException.class, () -> clienteService.cancelarCompra(50));
    }

    private void stubUsuarioYFuncion() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(funcionRepository.findByIdParaCompra(10)).thenReturn(Optional.of(funcion));
    }

    private CompraRequestDTO compra(Integer... asientoIds) {
        return new CompraRequestDTO(1, 10, List.of(asientoIds));
    }

    private TicketCompra ticketConfirmado() {
        TicketCompra ticket = new TicketCompra();
        ticket.setId(50);
        ticket.setUsuario(cliente);
        ticket.setFuncion(funcion);
        ticket.setEstado(EstadoCompra.CONFIRMADA);
        ticket.setTotal(funcion.getPrecio());
        Entrada entrada = new Entrada();
        entrada.setAsiento(a1);
        entrada.setEstado(EstadoEntrada.OCUPADA);
        entrada.setPrecioUnitario(funcion.getPrecio());
        ticket.agregarEntrada(entrada);
        return ticket;
    }

    private static Sala sala(Integer id, String nombre) {
        Sala s = new Sala();
        s.setId(id);
        s.setNombre(nombre);
        s.setCapacidad(40);
        return s;
    }

    private static Asiento asiento(Integer id, String fila, Integer numero, Sala sala) {
        Asiento a = new Asiento();
        a.setId(id);
        a.setFilaAsiento(fila);
        a.setNumeroAsiento(numero);
        a.setSala(sala);
        return a;
    }
}
