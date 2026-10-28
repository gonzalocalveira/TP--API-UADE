package com.example.GestionDeCine.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GestionDeCine.dto.AsientoRequestDTO;
import com.example.GestionDeCine.dto.AsientoResponseDTO;
import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.mapper.AsientoMapper;
import com.example.GestionDeCine.model.Asiento;
import com.example.GestionDeCine.model.Sala;
import com.example.GestionDeCine.repository.AsientoRepository;
import com.example.GestionDeCine.repository.EntradaRepository;
import com.example.GestionDeCine.repository.SalaRepository;
import com.example.GestionDeCine.service.interfaces.IAsientoService;

/** Logica de negocio de butacas: ubicacion unica dentro de la sala y respeto de la capacidad. */
@Service
public class AsientoService implements IAsientoService {

    private final AsientoRepository asientoRepository;
    private final SalaRepository salaRepository;
    private final EntradaRepository entradaRepository;

    public AsientoService(AsientoRepository asientoRepository, SalaRepository salaRepository,
                          EntradaRepository entradaRepository) {
        this.asientoRepository = asientoRepository;
        this.salaRepository = salaRepository;
        this.entradaRepository = entradaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsientoResponseDTO> obtenerAsientos() {
        return asientoRepository.findAll().stream().map(AsientoMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsientoResponseDTO> obtenerAsientosPorSala(Integer salaId) {
        buscarSala(salaId);
        return asientoRepository.findBySalaIdOrderByFilaAsientoAscNumeroAsientoAsc(salaId).stream()
                .map(AsientoMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AsientoResponseDTO obtenerAsientoPorId(Integer id) {
        return AsientoMapper.toResponse(buscarAsiento(id));
    }

    @Override
    @Transactional
    public AsientoResponseDTO crearAsiento(AsientoRequestDTO dto) {
        Sala sala = buscarSala(dto.getSalaId());
        validarUbicacionLibre(sala, dto);
        validarCapacidad(sala);

        Asiento asiento = new Asiento();
        copiarDatos(dto, sala, asiento);
        return AsientoMapper.toResponse(asientoRepository.save(asiento));
    }

    @Override
    @Transactional
    public AsientoResponseDTO modificarAsiento(Integer id, AsientoRequestDTO dto) {
        Asiento asiento = buscarAsiento(id);
        Sala sala = buscarSala(dto.getSalaId());

        boolean cambiaUbicacion = !Objects.equals(asiento.getSala().getId(), sala.getId())
                || !asiento.getFilaAsiento().equalsIgnoreCase(dto.getFilaAsiento().trim())
                || !Objects.equals(asiento.getNumeroAsiento(), dto.getNumeroAsiento());
        if (cambiaUbicacion) {
            validarUbicacionLibre(sala, dto);
        }
        if (!Objects.equals(asiento.getSala().getId(), sala.getId())) {
            validarCapacidad(sala);
        }

        copiarDatos(dto, sala, asiento);
        return AsientoMapper.toResponse(asientoRepository.save(asiento));
    }

    /** Una butaca que ya se vendio no se borra: las entradas la referencian. */
    @Override
    @Transactional
    public void eliminarAsiento(Integer id) {
        Asiento asiento = buscarAsiento(id);
        if (entradaRepository.existsByAsientoId(id)) {
            throw new BusinessException("No se puede eliminar la butaca " + id + " porque tiene entradas vendidas");
        }
        asientoRepository.delete(asiento);
    }

    private void validarUbicacionLibre(Sala sala, AsientoRequestDTO dto) {
        String fila = dto.getFilaAsiento().trim();
        if (asientoRepository.existsBySalaIdAndFilaAsientoIgnoreCaseAndNumeroAsiento(sala.getId(), fila, dto.getNumeroAsiento())) {
            throw new BusinessException("Ya existe la butaca " + fila + dto.getNumeroAsiento() + " en " + sala.getNombre());
        }
    }

    private void validarCapacidad(Sala sala) {
        if (asientoRepository.countBySalaId(sala.getId()) >= sala.getCapacidad()) {
            throw new BusinessException(sala.getNombre() + " ya tiene sus " + sala.getCapacidad() + " butacas (capacidad completa)");
        }
    }

    private void copiarDatos(AsientoRequestDTO dto, Sala sala, Asiento asiento) {
        asiento.setNumeroAsiento(dto.getNumeroAsiento());
        asiento.setFilaAsiento(dto.getFilaAsiento().trim().toUpperCase());
        asiento.setSala(sala);
    }

    private Asiento buscarAsiento(Integer id) {
        return asientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la butaca con id " + id));
    }

    private Sala buscarSala(Integer id) {
        return salaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la sala con id " + id));
    }
}
