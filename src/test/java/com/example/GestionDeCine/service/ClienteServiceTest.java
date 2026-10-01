package com.example.GestionDeCine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.model.Asiento;
import com.example.GestionDeCine.model.EstadoEntrada;
import com.example.GestionDeCine.model.Funcion;
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
 * Tests unitarios de ClienteService.comprarEntrada, la regla de negocio
 * mas importante del dominio. Se mockean los repositories (no se toca la
 * base de datos real) para poder probar cada camino de la logica de forma
 * aislada y rapida.
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

    private Usuario usuario;
    private Funcion funcion;
    private Asiento asiento;
    private Sala sala;

    @BeforeEach
    void setUp() {
        sala = new Sala(1, "Sala 1", 50);

        usuario = new Usuario();
        usuario.setId(1);

        funcion = new Funcion();
        funcion.setId(10);
        funcion.setSala(sala);
        funcion.setPrecio(3500.0);

        asiento = new Asiento();
        asiento.setId(100);
        asiento.setSala(sala);
    }

    @Test
    void comprarEntrada_conAsientoLibre_creaElTicket() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(funcionRepository.findById(10)).thenReturn(Optional.of(funcion));
        when(asientoRepository.findById(100)).thenReturn(Optional.of(asiento));
        when(entradaRepository.existsByFuncionIdAndAsientoIdAndEstado(10, 100, EstadoEntrada.OCUPADA))
                .thenReturn(false);
        when(entradaRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));
        when(ticketCompraRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

        TicketCompra resultado = clienteService.comprarEntrada(1, 10, 100);

        assertEquals(3500.0, resultado.getPrecio(), 0.001);
        assertEquals(EstadoEntrada.OCUPADA, resultado.getEntrada().getEstado());
    }

    @Test
    void comprarEntrada_conAsientoYaOcupado_lanzaBusinessException() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(funcionRepository.findById(10)).thenReturn(Optional.of(funcion));
        when(asientoRepository.findById(100)).thenReturn(Optional.of(asiento));
        when(entradaRepository.existsByFuncionIdAndAsientoIdAndEstado(10, 100, EstadoEntrada.OCUPADA))
                .thenReturn(true);

        assertThrows(BusinessException.class, () -> clienteService.comprarEntrada(1, 10, 100));
    }

    @Test
    void comprarEntrada_conAsientoDeOtraSala_lanzaBusinessException() {
        Sala otraSala = new Sala(2, "Sala 2", 30);
        asiento.setSala(otraSala);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(funcionRepository.findById(10)).thenReturn(Optional.of(funcion));
        when(asientoRepository.findById(100)).thenReturn(Optional.of(asiento));

        assertThrows(BusinessException.class, () -> clienteService.comprarEntrada(1, 10, 100));
    }

    @Test
    void comprarEntrada_conUsuarioInexistente_lanzaResourceNotFound() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clienteService.comprarEntrada(1, 10, 100));
    }
}
