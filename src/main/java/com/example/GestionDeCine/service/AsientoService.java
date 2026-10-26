package com.example.GestionDeCine.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.model.Asiento;
import com.example.GestionDeCine.model.Sala;
import com.example.GestionDeCine.repository.AsientoRepository;
import com.example.GestionDeCine.repository.SalaRepository;
import com.example.GestionDeCine.service.interfaces.IAsientoService;

/**
 * Se encarga de consultar, crear, modificar y eliminar asientos, validar sus datos y comprobar que pertenezcan a una sala existente.
 * Tambien verifica que no se repita la ubicacion de un asiento dentro de una misma sala.
 */
@Service
public class AsientoService implements IAsientoService {

    private final AsientoRepository asientoRepository;
    private final SalaRepository salaRepository;


    public AsientoService(
            AsientoRepository asientoRepository,
            SalaRepository salaRepository) {

        this.asientoRepository = asientoRepository;
        this.salaRepository = salaRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public List<Asiento> obtenerAsientos() {
        return asientoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asiento> obtenerAsientosPorSala(Integer salaId) {

        validarId(salaId, "sala");

        // Comprueba que la sala exista antes de buscar sus asientos.
        obtenerSalaPorId(salaId);

        return asientoRepository.findBySalaId(salaId);
    }

    @Override
    @Transactional(readOnly = true)
    public Asiento obtenerAsientoPorId(Integer id) {

        validarId(id, "asiento");

        return asientoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Asiento no encontrado con id " + id
                        )
                );
    }


    /**

     * Valida los datos recibidos, verifica que la sala exista y comprueba que no exista otro asiento con la misma fila
     * y numero dentro de esa sala.
     */
    @Override
    @Transactional
    public Asiento crearAsiento(Asiento asiento) {


        validarAsiento(asiento);


        Sala sala = obtenerSalaPorId(asiento.getSala().getId());


        boolean yaExiste = existeUbicacionDuplicada(
                sala.getId(),
                asiento.getFilaAsiento(),
                asiento.getNumeroAsiento(),
                null
        );

        if (yaExiste) {
            throw new BusinessException(
                    "Ya existe el asiento "
                            + asiento.getFilaAsiento()
                            + asiento.getNumeroAsiento()
                            + " en la sala "
                            + sala.getNombre()
            );
        }

        // El identificador se genera automaticamente.
        asiento.setId(null);

        // Asocia la entidad Sala recuperada de la base de datos.
        asiento.setSala(sala);

        return asientoRepository.save(asiento);
    }


    /**

     * Comprueba que el asiento exista, valida los nuevos datos,
     * verifica que la sala de destino exista y controla que no se duplique la ubicacion de otro asiento de esa sala.
     * Conserva el identificador original del asiento.
     */
    @Override
    @Transactional
    public Asiento modificarAsiento(Integer id, Asiento asiento) {


        Asiento existente = obtenerAsientoPorId(id);

        validarAsiento(asiento);

        Sala sala = obtenerSalaPorId(asiento.getSala().getId());


        boolean yaExiste = existeUbicacionDuplicada(
                sala.getId(),
                asiento.getFilaAsiento(),
                asiento.getNumeroAsiento(),
                existente.getId()
        );

        if (yaExiste) {
            throw new BusinessException(
                    "Ya existe el asiento "
                            + asiento.getFilaAsiento()
                            + asiento.getNumeroAsiento()
                            + " en la sala "
                            + sala.getNombre()
            );
        }

        // Actualiza los atributos modificables.
        existente.setNumeroAsiento(asiento.getNumeroAsiento());
        existente.setFilaAsiento(asiento.getFilaAsiento());
        existente.setSala(sala);

        return asientoRepository.save(existente);
    }



    /**

     * Primero verifica que el asiento exista y luego solicita su eliminacion al repositorio.
     * La base de datos podria impedir la eliminacion si hay entradas u otros registros asociados al asiento.
     */
    @Override
    @Transactional
    public void eliminarAsiento(Integer id) {

        Asiento asiento = obtenerAsientoPorId(id);

        asientoRepository.delete(asiento);
    }


    /**
     * Comprueba que el asiento tenga los datos obligatorios.
     * Verifica que:
     * - El objeto no sea nulo.
     * - El numero sea mayor a cero.
     * - La fila no sea nula ni este vacia.
     * - La sala tenga un identificador valido.
     * Tambien elimina los espacios innecesarios al principio y al final del nombre de la fila.
     */
    private void validarAsiento(Asiento asiento) {

        if (asiento == null) {
            throw new BusinessException(
                    "Los datos del asiento son obligatorios"
            );
        }

        if (asiento.getNumeroAsiento() == null) {
            throw new BusinessException(
                    "El numero de asiento es obligatorio"
            );
        }

        if (asiento.getNumeroAsiento() <= 0) {
            throw new BusinessException(
                    "El numero de asiento debe ser mayor a 0"
            );
        }

        if (asiento.getFilaAsiento() == null
                || asiento.getFilaAsiento().isBlank()) {
            throw new BusinessException(
                    "La fila del asiento es obligatoria"
            );
        }

        if (asiento.getSala() == null
                || asiento.getSala().getId() == null
                || asiento.getSala().getId() <= 0) {
            throw new BusinessException(
                    "El asiento debe pertenecer a una sala valida"
            );
        }
        asiento.setFilaAsiento(asiento.getFilaAsiento().trim());
    }


    private Sala obtenerSalaPorId(Integer salaId) {

        validarId(salaId, "sala");

        return salaRepository.findById(salaId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Sala no encontrada con id " + salaId
                        )
                );
    }

    /**
     * Comprueba si ya existe otro asiento con la misma fila y numero dentro de una sala.
     * asientoExcluirId permite excluir el asiento que se esta modificando. Si es null, se comprueban todos los asientos.
     */
    private boolean existeUbicacionDuplicada(
            Integer salaId,
            String fila,
            Integer numero,
            Integer asientoExcluirId) {

        return asientoRepository.findBySalaId(salaId).stream()
                .anyMatch(asiento ->
                        !Objects.equals(
                                asiento.getId(),
                                asientoExcluirId
                        )
                                && asiento.getFilaAsiento() != null
                                && asiento.getFilaAsiento()
                                .equalsIgnoreCase(fila)
                                && Objects.equals(
                                asiento.getNumeroAsiento(),
                                numero
                        )
                );
    }

    /**
     * Comprueba que el identificador sea valido.
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