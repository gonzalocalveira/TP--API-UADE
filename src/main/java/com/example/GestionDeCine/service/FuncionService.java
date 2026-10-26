package com.example.GestionDeCine.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.model.Asiento;
import com.example.GestionDeCine.model.EstadoEntrada;
import com.example.GestionDeCine.model.Funcion;
import com.example.GestionDeCine.model.Pelicula;
import com.example.GestionDeCine.model.Sala;
import com.example.GestionDeCine.repository.AsientoRepository;
import com.example.GestionDeCine.repository.EntradaRepository;
import com.example.GestionDeCine.repository.FuncionRepository;
import com.example.GestionDeCine.repository.PeliculaRepository;
import com.example.GestionDeCine.repository.SalaRepository;
import com.example.GestionDeCine.service.interfaces.IFuncionService;

/**

 * Se encarga de consultar, crear, modificar y eliminar funciones.
 * Tambien valida los datos y las relaciones con peliculas y salas.
 * Permite consultar los asientos disponibles para una funcion, considerando las entradas que se encuentran en estado OCUPADA.
 */
@Service
public class FuncionService implements IFuncionService {

    private final FuncionRepository funcionRepository;
    private final PeliculaRepository peliculaRepository;
    private final SalaRepository salaRepository;
    private final AsientoRepository asientoRepository;
    private final EntradaRepository entradaRepository;


    public FuncionService(
            FuncionRepository funcionRepository,
            PeliculaRepository peliculaRepository,
            SalaRepository salaRepository,
            AsientoRepository asientoRepository,
            EntradaRepository entradaRepository) {

        this.funcionRepository = funcionRepository;
        this.peliculaRepository = peliculaRepository;
        this.salaRepository = salaRepository;
        this.asientoRepository = asientoRepository;
        this.entradaRepository = entradaRepository;
    }



    @Override
    @Transactional(readOnly = true)
    public List<Funcion> obtenerFunciones() {
        return funcionRepository.findAll();
    }


    @Override
    @Transactional(readOnly = true)
    public Funcion obtenerFuncionPorId(Integer id) {

        validarId(id, "funcion");

        return funcionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Funcion no encontrada con id " + id
                        )
                );
    }


    /**
     * Registra una nueva funcion.
     * Valida los datos recibidos y comprueba que la pelicula y la sala asociadas existan en la base de datos.
     * El identificador se genera automaticamente.
     */
    @Override
    @Transactional
    public Funcion crearFuncion(Funcion funcion) {


        validarFuncion(funcion);


        Pelicula pelicula = obtenerPeliculaAsociada(funcion);
        Sala sala = obtenerSalaAsociada(funcion);


        funcion.setId(null);


        funcion.setPelicula(pelicula);
        funcion.setSala(sala);

        return funcionRepository.save(funcion);
    }



    /**
     * Modifica una funcion existente.
     * Verifica que la funcion exista, valida los nuevos datos y comprueba que las asociaciones con pelicula y sala sean validas.
     * Conserva el identificador original de la funcion.
     */
    @Override
    @Transactional
    public Funcion modificarFuncion(Integer id, Funcion funcion) {

        Funcion existente = obtenerFuncionPorId(id);


        validarFuncion(funcion);


        Pelicula pelicula = obtenerPeliculaAsociada(funcion);
        Sala sala = obtenerSalaAsociada(funcion);
        existente.setFechaFuncion(funcion.getFechaFuncion());
        existente.setHoraFuncion(funcion.getHoraFuncion());
        existente.setFormatoFuncion(funcion.getFormatoFuncion());
        existente.setPrecio(funcion.getPrecio());
        existente.setPelicula(pelicula);
        existente.setSala(sala);

        return funcionRepository.save(existente);
    }

    /**
     * Elimina una funcion existente.
     * Primero verifica que la funcion exista.
     * La base de datos podria impedir la eliminacion si hay entradas u otros registros asociados.
     */
    @Override
    @Transactional
    public void eliminarFuncion(Integer id) {

        Funcion funcion = obtenerFuncionPorId(id);

        funcionRepository.delete(funcion);
    }

    /**
     * Obtiene los asientos de la sala asignada a una funcion que no tienen una entrada en estado OCUPADA para esa funcion.
     * Una entrada CANCELADA no bloquea el asiento.
     * La disponibilidad se calcula para cada funcion, ya que un mismo asiento puede estar ocupado en una funcion y libre en otra.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Asiento> obtenerAsientosDisponibles(Integer funcionId) {

        // Verifica que la funcion exista.
        Funcion funcion = obtenerFuncionPorId(funcionId);

        // Recupera los asientos pertenecientes a la sala de la funcion.
        List<Asiento> asientosDeLaSala =
                asientoRepository.findBySalaId(
                        funcion.getSala().getId()
                );

        // Excluye los asientos que tengan una entrada OCUPADA en la funcion consultada.
        return asientosDeLaSala.stream()
                .filter(asiento ->
                        !entradaRepository
                                .existsByFuncionIdAndAsientoIdAndEstado(
                                        funcionId,
                                        asiento.getId(),
                                        EstadoEntrada.OCUPADA
                                )
                )
                .toList();
    }

    /**
     * Comprueba que la funcion tenga todos los datos obligatorios: fecha, horario, formato, precio, pelicula y sala.
     * El formato debe corresponder a un valor del enum FormatoFuncion: DOS_D, TRES_D o CUATRO_DX.
     */
    private void validarFuncion(Funcion funcion) {

        if (funcion == null) {
            throw new BusinessException(
                    "Los datos de la funcion son obligatorios"
            );
        }

        if (funcion.getFechaFuncion() == null) {
            throw new BusinessException(
                    "La fecha de la funcion es obligatoria"
            );
        }

        if (funcion.getHoraFuncion() == null) {
            throw new BusinessException(
                    "El horario de la funcion es obligatorio"
            );
        }

        if (funcion.getFormatoFuncion() == null) {
            throw new BusinessException(
                    "El formato de la funcion es obligatorio"
            );
        }

        if (funcion.getPrecio() == null) {
            throw new BusinessException(
                    "El precio de la entrada es obligatorio"
            );
        }

        // Evita precios iguales a cero, negativos, infinitos o NaN.
        if (!Double.isFinite(funcion.getPrecio())
                || funcion.getPrecio() <= 0) {
            throw new BusinessException(
                    "El precio debe ser mayor a 0"
            );
        }

        // Comprueba que se haya informado una pelicula con ID valido.
        if (funcion.getPelicula() == null
                || funcion.getPelicula().getId() == null
                || funcion.getPelicula().getId() <= 0) {
            throw new BusinessException(
                    "La funcion debe tener una pelicula valida"
            );
        }

        if (funcion.getSala() == null
                || funcion.getSala().getId() == null
                || funcion.getSala().getId() <= 0) {
            throw new BusinessException(
                    "La funcion debe tener una sala valida"
            );
        }
    }


    private Pelicula obtenerPeliculaAsociada(Funcion funcion) {

        Integer peliculaId = funcion.getPelicula().getId();

        return peliculaRepository.findById(peliculaId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pelicula no encontrada con id "
                                        + peliculaId
                        )
                );
    }

    /**
     * Busca la sala indicada en los datos de la funcion.
     */
    private Sala obtenerSalaAsociada(Funcion funcion) {

        Integer salaId = funcion.getSala().getId();

        return salaRepository.findById(salaId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Sala no encontrada con id " + salaId
                        )
                );
    }

    /**
     * Comprueba que el identificador sea valido.
     * No acepta IDs nulos, iguales a cero o negativos.
     */
    private void validarId(Integer id, String entidad) {

        if (id == null || id <= 0) {
            throw new ResourceNotFoundException(
                    "Identificador de " + entidad
                            + " invalido: " + id
            );
        }
    }
}