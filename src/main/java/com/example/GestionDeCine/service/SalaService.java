package com.example.GestionDeCine.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.model.Sala;
import com.example.GestionDeCine.repository.SalaRepository;
import com.example.GestionDeCine.service.interfaces.ISalaService;

/**
 * Se encarga de consultar, crear, modificar y eliminar salas, validando los datos antes de guardarlos en la base de datos.
 * La clase utiliza SalaRepository para acceder a la persistencia y delega en el controlador la gestion de las solicitudes HTTP.
 */
@Service
public class SalaService implements ISalaService {

    private final SalaRepository salaRepository;


    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }



    @Override
    @Transactional(readOnly = true)
    public List<Sala> obtenerSalas() {
        return salaRepository.findAll();
    }

    /**
     * Busca una sala por su identificador.
     */
    @Override
    @Transactional(readOnly = true)
    public Sala obtenerSalaPorId(Integer id) {

        validarId(id);

        return salaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Sala no encontrada con id " + id
                        )
                );
    }

    /**
     * Registra una nueva sala.
     * Primero valida los datos recibidos y luego guarda la sala.
     * El ID se genera automaticamente mediante la base de datos.
     */
    @Override
    @Transactional
    public Sala crearSala(Sala sala) {

        validarSala(sala);

        // Evita utilizar un ID enviado en la solicitud.
        sala.setId(null);
        return salaRepository.save(sala);
    }


    /**
     * Modifica los datos de una sala existente.
     * Verifica que la sala exista y que los nuevos datos sean validos.
     * Solamente actualiza el nombre y la capacidad, conservando el ID.
     */
    @Override
    @Transactional
    public Sala modificarSala(Integer id, Sala sala) {


        Sala existente = obtenerSalaPorId(id);

        validarSala(sala);

        existente.setNombre(sala.getNombre());
        existente.setCapacidad(sala.getCapacidad());

        return salaRepository.save(existente);
    }

    /**
     * Primero busca la sala para comprobar que exista.
     * La base de datos puede impedir la eliminacion si existen
     * registros relacionados con esta sala.
     */
    @Override
    @Transactional
    public void eliminarSala(Integer id) {

        Sala sala = obtenerSalaPorId(id);

        salaRepository.delete(sala);
    }


    /**
     * Verifica que la sala recibida tenga los datos obligatorios.
     * Comprueba que:
     * - El objeto no sea nulo.
     * - El nombre no sea nulo ni este vacio.
     * - La capacidad este informada y sea mayor a cero.
     * Tambien elimina los espacios al principio y al final
     * del nombre de la sala.
     */
    private void validarSala(Sala sala) {

        if (sala == null) {
            throw new BusinessException(
                    "Los datos de la sala son obligatorios"
            );
        }

        if (sala.getNombre() == null
                || sala.getNombre().isBlank()) {
            throw new BusinessException(
                    "El nombre de la sala es obligatorio"
            );
        }

        if (sala.getCapacidad() == null) {
            throw new BusinessException(
                    "La capacidad de la sala es obligatoria"
            );
        }

        if (sala.getCapacidad() <= 0) {
            throw new BusinessException(
                    "La capacidad debe ser mayor a 0"
            );
        }

        // Normaliza el nombre antes de persistirlo.
        sala.setNombre(sala.getNombre().trim());
    }

    /**
     * Comprueba que el identificador sea valido.
     * No se aceptan IDs nulos, iguales a cero o negativos.
     */
    private void validarId(Integer id) {

        if (id == null || id <= 0) {
            throw new ResourceNotFoundException(
                    "Sala no encontrada con id " + id
            );
        }
    }
}