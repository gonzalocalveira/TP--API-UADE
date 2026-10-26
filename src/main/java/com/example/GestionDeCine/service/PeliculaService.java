package com.example.GestionDeCine.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.model.Pelicula;
import com.example.GestionDeCine.repository.PeliculaRepository;
import com.example.GestionDeCine.service.interfaces.IPeliculaService;

/**

 * Se encarga de consultar, crear, modificar y eliminar peliculas, validando sus datos antes de guardarlos en la base de datos.

 */
@Service
public class PeliculaService implements IPeliculaService {

    private final PeliculaRepository peliculaRepository;


    public PeliculaService(PeliculaRepository peliculaRepository) {
        this.peliculaRepository = peliculaRepository;
    }



    /**
     * Obtiene todas las peliculas registradas en la base de datos.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Pelicula> obtenerPeliculas() {
        return peliculaRepository.findAll();
    }

    /**
     * Busca una pelicula por su identificador.
     */
    @Override
    @Transactional(readOnly = true)
    public Pelicula obtenerPeliculaPorId(Integer id) {

        validarId(id);

        return peliculaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pelicula no encontrada con id " + id
                        )
                );
    }


    /**
     * Registra una nueva pelicula.

     */
    @Override
    @Transactional
    public Pelicula crearPelicula(Pelicula pelicula) {

        validarPelicula(pelicula);

        pelicula.setId(null);

        return peliculaRepository.save(pelicula);
    }



    /**
     * Modifica una pelicula existente.
     * Primero verifica que la pelicula exista y que los datos recibidos sean validos.
     * Actualiza los atributos modificables y conserva el IDmoriginal de la pelicula.
     */
    @Override
    @Transactional
    public Pelicula modificarPelicula(Integer id, Pelicula pelicula) {

        // Busca la pelicula y comprueba que exista.
        Pelicula existente = obtenerPeliculaPorId(id);

        // Valida los datos antes de realizar la modificacion.
        validarPelicula(pelicula);

        // Actualiza los atributos de la pelicula existente.
        existente.setNombre(pelicula.getNombre());
        existente.setDirector(pelicula.getDirector());
        existente.setDuracion(pelicula.getDuracion());
        existente.setFechaEstreno(pelicula.getFechaEstreno());
        existente.setIdioma(pelicula.getIdioma());
        existente.setGenero(pelicula.getGenero());
        existente.setClasificacionEdad(pelicula.getClasificacionEdad());
        existente.setSinopsis(pelicula.getSinopsis());

        return peliculaRepository.save(existente);
    }


    /**
     * Elimina una pelicula existente.
     * Primero busca la pelicula para verificar que exista.
     * Si no existe, se lanza ResourceNotFoundException.
     * La base de datos podria impedir la eliminacion si existenmfunciones u otros registros asociados a la pelicula.
     */
    @Override
    @Transactional
    public void eliminarPelicula(Integer id) {

        Pelicula pelicula = obtenerPeliculaPorId(id);

        peliculaRepository.delete(pelicula);
    }

    /**
     * Comprueba que la pelicula tenga los datos obligatorios.
     *
     * Verifica que:
     * - El objeto no sea nulo.
     * - El nombre y el director no esten vacios.
     * - La duracion sea mayor a cero.
     * - La fecha de estreno este informada.
     * - El idioma, genero y clasificacion por edad sean validos.
     *
     * Tambien elimina espacios innecesarios al principio y al final de los campos de texto obligatorios.
     * La sinopsis es opcional.
     */
    private void validarPelicula(Pelicula pelicula) {

        if (pelicula == null) {
            throw new BusinessException(
                    "Los datos de la pelicula son obligatorios"
            );
        }

        if (pelicula.getNombre() == null
                || pelicula.getNombre().isBlank()) {
            throw new BusinessException(
                    "El nombre de la pelicula es obligatorio"
            );
        }

        if (pelicula.getDirector() == null
                || pelicula.getDirector().isBlank()) {
            throw new BusinessException(
                    "El director es obligatorio"
            );
        }

        if (pelicula.getDuracion() == null) {
            throw new BusinessException(
                    "La duracion de la pelicula es obligatoria"
            );
        }

        if (pelicula.getDuracion() <= 0) {
            throw new BusinessException(
                    "La duracion debe ser mayor a 0"
            );
        }

        if (pelicula.getFechaEstreno() == null) {
            throw new BusinessException(
                    "La fecha de estreno es obligatoria"
            );
        }

        if (pelicula.getIdioma() == null
                || pelicula.getIdioma().isBlank()) {
            throw new BusinessException(
                    "El idioma es obligatorio"
            );
        }

        if (pelicula.getGenero() == null
                || pelicula.getGenero().isBlank()) {
            throw new BusinessException(
                    "El genero es obligatorio"
            );
        }

        if (pelicula.getClasificacionEdad() == null
                || pelicula.getClasificacionEdad().isBlank()) {
            throw new BusinessException(
                    "La clasificacion por edad es obligatoria"
            );
        }


        pelicula.setNombre(pelicula.getNombre().trim());
        pelicula.setDirector(pelicula.getDirector().trim());
        pelicula.setIdioma(pelicula.getIdioma().trim());
        pelicula.setGenero(pelicula.getGenero().trim());
        pelicula.setClasificacionEdad(
                pelicula.getClasificacionEdad().trim()
        );


        if (pelicula.getSinopsis() == null
                || pelicula.getSinopsis().isBlank()) {
            throw new BusinessException(
                    "La sinopsis es obligatoria"
            );
        }
        pelicula.setSinopsis(pelicula.getSinopsis().trim());
    }

    /**
     * Comprueba que el identificador sea valido.
     * No se aceptan IDs nulos, iguales a cero o negativos.
     */
    private void validarId(Integer id) {

        if (id == null || id <= 0) {
            throw new ResourceNotFoundException(
                    "Pelicula no encontrada con id " + id
            );
        }
    }
}