package com.example.GestionDeCine.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GestionDeCine.dto.SalaRequestDTO;
import com.example.GestionDeCine.dto.SalaResponseDTO;
import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.mapper.SalaMapper;
import com.example.GestionDeCine.model.Sala;
import com.example.GestionDeCine.repository.AsientoRepository;
import com.example.GestionDeCine.repository.FuncionRepository;
import com.example.GestionDeCine.repository.SalaRepository;
import com.example.GestionDeCine.service.interfaces.ISalaService;

/** Logica de negocio de salas: nombre unico, capacidad coherente con las butacas y borrado seguro. */
@Service
public class SalaService implements ISalaService {

    private final SalaRepository salaRepository;
    private final AsientoRepository asientoRepository;
    private final FuncionRepository funcionRepository;

    public SalaService(SalaRepository salaRepository, AsientoRepository asientoRepository,
                       FuncionRepository funcionRepository) {
        this.salaRepository = salaRepository;
        this.asientoRepository = asientoRepository;
        this.funcionRepository = funcionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalaResponseDTO> obtenerSalas() {
        return salaRepository.findAll().stream().map(SalaMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SalaResponseDTO obtenerSalaPorId(Integer id) {
        return SalaMapper.toResponse(buscarSala(id));
    }

    @Override
    @Transactional
    public SalaResponseDTO crearSala(SalaRequestDTO dto) {
        if (salaRepository.existsByNombreIgnoreCase(dto.getNombre().trim())) {
            throw new BusinessException("Ya existe una sala con el nombre " + dto.getNombre());
        }
        Sala sala = new Sala();
        SalaMapper.copiarDatos(dto, sala);
        return SalaMapper.toResponse(salaRepository.save(sala));
    }

    /** No permite repetir el nombre de otra sala ni bajar la capacidad por debajo de las butacas cargadas. */
    @Override
    @Transactional
    public SalaResponseDTO modificarSala(Integer id, SalaRequestDTO dto) {
        Sala sala = buscarSala(id);
        if (salaRepository.existsByNombreIgnoreCaseAndIdNot(dto.getNombre().trim(), id)) {
            throw new BusinessException("Ya existe otra sala con el nombre " + dto.getNombre());
        }
        long butacas = asientoRepository.countBySalaId(id);
        if (dto.getCapacidad() < butacas) {
            throw new BusinessException("La capacidad no puede ser menor a las " + butacas + " butacas que ya tiene la sala");
        }
        SalaMapper.copiarDatos(dto, sala);
        return SalaMapper.toResponse(salaRepository.save(sala));
    }

    @Override
    @Transactional
    public void eliminarSala(Integer id) {
        Sala sala = buscarSala(id);
        if (asientoRepository.existsBySalaId(id) || funcionRepository.existsBySalaId(id)) {
            throw new BusinessException("No se puede eliminar la sala " + id + " porque tiene butacas o funciones asociadas");
        }
        salaRepository.delete(sala);
    }

    private Sala buscarSala(Integer id) {
        return salaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la sala con id " + id));
    }
}
