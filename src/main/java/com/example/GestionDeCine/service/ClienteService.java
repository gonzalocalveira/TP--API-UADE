package com.example.GestionDeCine.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GestionDeCine.dto.CompraRequestDTO;
import com.example.GestionDeCine.dto.MembresiaResponseDTO;
import com.example.GestionDeCine.dto.PeliculaResponseDTO;
import com.example.GestionDeCine.dto.TicketCompraResponseDTO;
import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.mapper.CompraMapper;
import com.example.GestionDeCine.mapper.MembresiaMapper;
import com.example.GestionDeCine.mapper.PeliculaMapper;
import com.example.GestionDeCine.model.Asiento;
import com.example.GestionDeCine.model.Entrada;
import com.example.GestionDeCine.model.EstadoCompra;
import com.example.GestionDeCine.model.EstadoEntrada;
import com.example.GestionDeCine.model.Funcion;
import com.example.GestionDeCine.model.Rol;
import com.example.GestionDeCine.model.TicketCompra;
import com.example.GestionDeCine.model.Usuario;
import com.example.GestionDeCine.repository.AsientoRepository;
import com.example.GestionDeCine.repository.EntradaRepository;
import com.example.GestionDeCine.repository.FuncionRepository;
import com.example.GestionDeCine.repository.MembresiaRepository;
import com.example.GestionDeCine.repository.PeliculaRepository;
import com.example.GestionDeCine.repository.TicketCompraRepository;
import com.example.GestionDeCine.repository.UsuarioRepository;
import com.example.GestionDeCine.service.interfaces.IClienteService;

/**
 * Acciones de un cliente: ver la cartelera, comprar entradas, cancelar una
 * compra y consultar su historial y su membresia. Aca esta la regla mas
 * importante del sistema: nunca vender dos veces la misma butaca para la
 * misma funcion.
 */
@Service
public class ClienteService implements IClienteService {

    private final TicketCompraRepository ticketCompraRepository;
    private final MembresiaRepository membresiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FuncionRepository funcionRepository;
    private final AsientoRepository asientoRepository;
    private final EntradaRepository entradaRepository;
    private final PeliculaRepository peliculaRepository;

    public ClienteService(TicketCompraRepository ticketCompraRepository, MembresiaRepository membresiaRepository,
                          UsuarioRepository usuarioRepository, FuncionRepository funcionRepository,
                          AsientoRepository asientoRepository, EntradaRepository entradaRepository,
                          PeliculaRepository peliculaRepository) {
        this.ticketCompraRepository = ticketCompraRepository;
        this.membresiaRepository = membresiaRepository;
        this.usuarioRepository = usuarioRepository;
        this.funcionRepository = funcionRepository;
        this.asientoRepository = asientoRepository;
        this.entradaRepository = entradaRepository;
        this.peliculaRepository = peliculaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeliculaResponseDTO> verCartelera() {
        return peliculaRepository.findAll().stream().map(PeliculaMapper::toResponse).toList();
    }

    /**
     * Compra una o mas butacas para una funcion. Todo ocurre en una sola transaccion:
     * si cualquier regla falla, no se guarda nada (ni el ticket ni ninguna entrada).
     * Reglas, en orden:
     * 1. El usuario existe y es CLIENTE.
     * 2. La funcion existe (y se bloquea su fila hasta terminar, para evitar ventas simultaneas).
     * 3. La funcion todavia no empezo.
     * 4. No hay butacas repetidas en el pedido.
     * 5. Cada butaca existe y pertenece a la sala de la funcion.
     * 6. Ninguna butaca esta ocupada para esa funcion.
     * 7. El total = precio de la funcion x cantidad de butacas (lo calcula el servidor).
     */
    @Override
    @Transactional
    public TicketCompraResponseDTO comprarEntradas(CompraRequestDTO dto) {
        Usuario usuario = buscarCliente(dto.getUsuarioId());

        Funcion funcion = funcionRepository.findByIdParaCompra(dto.getFuncionId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe la funcion con id " + dto.getFuncionId()));
        validarQueNoEmpezo(funcion);

        List<Integer> asientoIds = dto.getAsientoIds();
        if (new HashSet<>(asientoIds).size() != asientoIds.size()) {
            throw new BusinessException("No se puede elegir la misma butaca dos veces en una compra");
        }

        Set<Integer> ocupadas = new HashSet<>(
                entradaRepository.findAsientoIdsByFuncionIdAndEstado(funcion.getId(), EstadoEntrada.OCUPADA));

        TicketCompra ticket = new TicketCompra();
        for (Integer asientoId : asientoIds) {
            Asiento asiento = asientoRepository.findById(asientoId)
                    .orElseThrow(() -> new ResourceNotFoundException("No existe la butaca con id " + asientoId));
            if (!Objects.equals(asiento.getSala().getId(), funcion.getSala().getId())) {
                throw new BusinessException("La butaca " + asientoId + " no pertenece a la sala de la funcion");
            }
            if (ocupadas.contains(asientoId)) {
                throw new BusinessException("La butaca " + asiento.getFilaAsiento() + asiento.getNumeroAsiento()
                        + " ya esta ocupada para esta funcion");
            }
            Entrada entrada = new Entrada();
            entrada.setAsiento(asiento);
            entrada.setEstado(EstadoEntrada.OCUPADA);
            entrada.setPrecioUnitario(funcion.getPrecio());
            ticket.agregarEntrada(entrada);
        }

        ticket.setUsuario(usuario);
        ticket.setFuncion(funcion);
        ticket.setFechaCompra(LocalDateTime.now());
        ticket.setEstado(EstadoCompra.CONFIRMADA);
        ticket.setTotal(funcion.getPrecio().multiply(BigDecimal.valueOf(asientoIds.size())));

        // cascade = ALL en TicketCompra.entradas: este save inserta el ticket y todas sus entradas.
        return CompraMapper.toResponse(ticketCompraRepository.save(ticket));
    }

    /**
     * Cancela una compra: solo si esta CONFIRMADA y la funcion no empezo. La compra no se
     * borra (queda en el historial); sus entradas pasan a CANCELADA y las butacas se liberan.
     */
    @Override
    @Transactional
    public TicketCompraResponseDTO cancelarCompra(Integer ticketId) {
        TicketCompra ticket = ticketCompraRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la compra con id " + ticketId));
        if (ticket.getEstado() == EstadoCompra.CANCELADA) {
            throw new BusinessException("La compra " + ticketId + " ya estaba cancelada");
        }
        validarQueNoEmpezo(ticket.getFuncion());

        ticket.setEstado(EstadoCompra.CANCELADA);
        ticket.getEntradas().forEach(entrada -> entrada.setEstado(EstadoEntrada.CANCELADA));
        return CompraMapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketCompraResponseDTO> verHistorial(Integer usuarioId) {
        buscarCliente(usuarioId);
        return ticketCompraRepository.findByUsuarioIdOrderByFechaCompraDesc(usuarioId).stream()
                .map(CompraMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MembresiaResponseDTO verMembresia(Integer usuarioId) {
        buscarCliente(usuarioId);
        return membresiaRepository.findByUsuarioId(usuarioId)
                .map(MembresiaMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario " + usuarioId + " no tiene una membresia"));
    }

    private void validarQueNoEmpezo(Funcion funcion) {
        if (!funcion.getInicio().isAfter(LocalDateTime.now())) {
            throw new BusinessException("La funcion " + funcion.getId() + " ya empezo o ya paso");
        }
    }

    private Usuario buscarCliente(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario con id " + usuarioId));
        if (usuario.getRol() != Rol.CLIENTE) {
            throw new BusinessException("Esta operacion solo esta permitida para clientes");
        }
        return usuario;
    }
}
