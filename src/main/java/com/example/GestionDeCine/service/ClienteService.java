package com.example.GestionDeCine.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
 * Se encarga de consultar la cartelera, consultar el historial de compras, consultar la membresia y comprar entradas para las funciones del cine.
 * Utiliza los repositorios para recuperar y guardar la informacion.
 * Las operaciones de compra se ejecutan dentro de una transaccion para evitar guardar parcialmente una compra si ocurre un error.
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


    public ClienteService(
            TicketCompraRepository ticketCompraRepository,
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
    @Transactional(readOnly = true)
    public List<TicketCompra> verHistorial(Integer usuarioId) {

        Usuario usuario = obtenerUsuarioPorId(usuarioId);

        validarCliente(usuario);

        return ticketCompraRepository.findByUsuarioId(usuarioId);
    }


    @Override
    @Transactional(readOnly = true)
    public Membresia verMembresia(Integer usuarioId) {

        Usuario usuario = obtenerUsuarioPorId(usuarioId);

        validarCliente(usuario);

        return membresiaRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "El usuario " + usuarioId
                                        + " no tiene una membresia asociada"
                        )
                );
    }



    /**
     * Registra la compra de una entrada para una funcion determinada.
     * Pasos:
     * 1. Valida los identificadores recibidos.
     * 2. Comprueba que el usuario exista y sea un cliente.
     * 3. Comprueba que la funcion y el asiento existan.
     * 4. Verifica que el asiento pertenezca a la sala de la funcion.
     * 5. Comprueba que el asiento no este ocupado para esa funcion.
     * 6. Crea la entrada con estado OCUPADA.
     * 7. Registra el ticket de compra con su fecha y precio.
     * @Transactional permite revertir las operaciones de persistencia si se produce un error durante el registro de la compra.
     */
    @Override
    @Transactional
    public TicketCompra comprarEntrada(
            Integer usuarioId,
            Integer funcionId,
            Integer asientoId) {


        validarId(usuarioId, "usuario");
        validarId(funcionId, "funcion");
        validarId(asientoId, "asiento");


        Usuario usuario = obtenerUsuarioPorId(usuarioId);
        validarCliente(usuario);


        Funcion funcion = obtenerFuncionPorId(funcionId);


        Asiento asiento = obtenerAsientoPorId(asientoId);


        if (funcion.getSala() == null
                || funcion.getSala().getId() == null) {
            throw new BusinessException(
                    "La funcion no tiene una sala valida asociada"
            );
        }


        if (asiento.getSala() == null
                || asiento.getSala().getId() == null) {
            throw new BusinessException(
                    "El asiento no tiene una sala valida asociada"
            );
        }


        if (!Objects.equals(
                asiento.getSala().getId(),
                funcion.getSala().getId())) {

            throw new BusinessException(
                    "El asiento seleccionado no pertenece "
                            + "a la sala de la funcion"
            );
        }


        if (funcion.getPrecio() == null
                || !Double.isFinite(funcion.getPrecio())
                || funcion.getPrecio() <= 0) {

            throw new BusinessException(
                    "La funcion no tiene un precio valido"
            );
        }


        boolean ocupado =
                entradaRepository.existsByFuncionIdAndAsientoIdAndEstado(
                        funcionId,
                        asientoId,
                        EstadoEntrada.OCUPADA
                );

        if (ocupado) {
            throw new BusinessException(
                    "El asiento ya esta ocupado para esta funcion"
            );
        }

        // Crea la entrada asociada a la funcion y al asiento.
        // Se marca como OCUPADA porque se esta registrando la compra.
        Entrada entrada = new Entrada();
        entrada.setFuncion(funcion);
        entrada.setAsiento(asiento);
        entrada.setEstado(EstadoEntrada.OCUPADA);


        entrada = entradaRepository.save(entrada);

        // Registra el ticket que representa la compra realizada.
        TicketCompra ticketCompra = new TicketCompra();
        ticketCompra.setUsuario(usuario);
        ticketCompra.setEntrada(entrada);
        ticketCompra.setFechaCompra(LocalDateTime.now());
        ticketCompra.setPrecio(funcion.getPrecio());

        return ticketCompraRepository.save(ticketCompra);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Pelicula> verCartelera() {
        return peliculaRepository.findAll();
    }



    /**
     * Busca un usuario por su identificador.

     */
    private Usuario obtenerUsuarioPorId(Integer id) {

        validarId(id, "usuario");

        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario no encontrado con id " + id
                        )
                );
    }

    /**
     * Busca una funcion por su identificador.
     */
    private Funcion obtenerFuncionPorId(Integer id) {

        validarId(id, "funcion");

        return funcionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Funcion no encontrada con id " + id
                        )
                );
    }

    /**
     * Busca un asiento por su identificador.
     */
    private Asiento obtenerAsientoPorId(Integer id) {

        validarId(id, "asiento");

        return asientoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Asiento no encontrado con id " + id
                        )
                );
    }


    private void validarCliente(Usuario usuario) {

        if (usuario.getRol() != Rol.CLIENTE) {
            throw new BusinessException(
                    "Esta operacion solo esta permitida para clientes"
            );
        }
    }


    private void validarId(Integer id, String entidad) {

        if (id == null || id <= 0) {
            throw new ResourceNotFoundException(
                    "Identificador de " + entidad + " invalido: " + id
            );
        }
    }
}