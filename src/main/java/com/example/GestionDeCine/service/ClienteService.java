package com.example.GestionDeCine.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.model.Asiento;
import com.example.GestionDeCine.model.Entrada;
import com.example.GestionDeCine.model.EstadoEntrada;
import com.example.GestionDeCine.model.Funcion;
import com.example.GestionDeCine.model.Membresia;
import com.example.GestionDeCine.model.Pelicula;
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
 * Logica de negocio de las operaciones que realiza un cliente. Se mantiene
 * separada de UsuarioService porque responde a una responsabilidad
 * distinta (acciones de e-commerce del cliente) y no a la gestion de la
 * cuenta de usuario en si.
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

    public ClienteService(TicketCompraRepository ticketCompraRepository,
                           MembresiaRepository membresiaRepository,
                           UsuarioRepository usuarioRepository,
                           FuncionRepository funcionRepository,
                           AsientoRepository asientoRepository,
                           EntradaRepository entradaRepository,
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
    public List<TicketCompra> verHistorial(Integer usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario no encontrado con id " + usuarioId);
        }
        return ticketCompraRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public Membresia verMembresia(Integer usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario no encontrado con id " + usuarioId);
        }
        return membresiaRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario " + usuarioId + " no tiene una membresia asociada"));
    }

    /**
     * Regla de negocio central del dominio: comprar una entrada.
     * Pasos (y por que estan en este orden):
     *  1. Validar que usuario, funcion y asiento existan (404 si no).
     *  2. Validar que el asiento pertenezca a la sala de esa funcion
     *     (no tendria sentido vender el asiento de una sala para una
     *     funcion que se proyecta en otra sala).
     *  3. Validar que ese asiento no este ya vendido para esa funcion
     *     puntual (evita "doble venta" del mismo lugar).
     *  4. Recien ahi se crea la Entrada (OCUPADA) y el TicketCompra.
     * @Transactional asegura que si algo falla a mitad de camino (por
     * ejemplo, al guardar el TicketCompra) la Entrada creada en el paso 4
     * tambien se revierte, evitando dejar datos inconsistentes.
     */
    @Override
    @Transactional
    public TicketCompra comprarEntrada(Integer usuarioId, Integer funcionId, Integer asientoId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + usuarioId));

        Funcion funcion = funcionRepository.findById(funcionId)
                .orElseThrow(() -> new ResourceNotFoundException("Funcion no encontrada con id " + funcionId));

        Asiento asiento = asientoRepository.findById(asientoId)
                .orElseThrow(() -> new ResourceNotFoundException("Asiento no encontrado con id " + asientoId));

        if (!asiento.getSala().getId().equals(funcion.getSala().getId())) {
            throw new BusinessException("El asiento seleccionado no pertenece a la sala de la funcion");
        }

        boolean ocupado = entradaRepository.existsByFuncionIdAndAsientoIdAndEstado(
                funcionId, asientoId, EstadoEntrada.OCUPADA);
        if (ocupado) {
            throw new BusinessException("El asiento ya esta ocupado para esta funcion");
        }

        Entrada entrada = new Entrada();
        entrada.setFuncion(funcion);
        entrada.setAsiento(asiento);
        entrada.setEstado(EstadoEntrada.OCUPADA);
        entrada = entradaRepository.save(entrada);

        TicketCompra ticketCompra = new TicketCompra();
        ticketCompra.setUsuario(usuario);
        ticketCompra.setEntrada(entrada);
        ticketCompra.setFechaCompra(LocalDateTime.now());
        ticketCompra.setPrecio(funcion.getPrecio());

        return ticketCompraRepository.save(ticketCompra);
    }

    @Override
    public List<Pelicula> verCartelera() {
        return peliculaRepository.findAll();
    }
}
