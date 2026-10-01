package com.example.GestionDeCine.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.model.Asiento;
import com.example.GestionDeCine.model.Sala;
import com.example.GestionDeCine.repository.AsientoRepository;
import com.example.GestionDeCine.repository.SalaRepository;
import com.example.GestionDeCine.service.interfaces.IAsientoService;

@Service
public class AsientoService implements IAsientoService {

    private final AsientoRepository asientoRepository;
    private final SalaRepository salaRepository;

    public AsientoService(AsientoRepository asientoRepository, SalaRepository salaRepository) {
        this.asientoRepository = asientoRepository;
        this.salaRepository = salaRepository;
    }

    @Override
    public List<Asiento> obtenerAsientos() {
        return asientoRepository.findAll();
    }

    @Override
    public List<Asiento> obtenerAsientosPorSala(Integer salaId) {
        if (!salaRepository.existsById(salaId)) {
            throw new ResourceNotFoundException("Sala no encontrada con id " + salaId);
        }
        return asientoRepository.findBySalaId(salaId);
    }

    @Override
    public Asiento obtenerAsientoPorId(Integer id) {
        return asientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asiento no encontrado con id " + id));
    }

    @Override
    public Asiento crearAsiento(Asiento asiento) {
        Sala sala = salaRepository.findById(asiento.getSala().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada con id " + asiento.getSala().getId()));
        asiento.setSala(sala);

        boolean yaExiste = asientoRepository.findBySalaId(sala.getId()).stream()
                .anyMatch(a -> a.getFilaAsiento().equalsIgnoreCase(asiento.getFilaAsiento())
                        && a.getNumeroAsiento().equals(asiento.getNumeroAsiento()));
        if (yaExiste) {
            throw new BusinessException("Ya existe el asiento " + asiento.getFilaAsiento()
                    + asiento.getNumeroAsiento() + " en la sala " + sala.getNombre());
        }

        return asientoRepository.save(asiento);
    }

    @Override
    public Asiento modificarAsiento(Integer id, Asiento asiento) {
        Asiento existente = obtenerAsientoPorId(id);
        existente.setNumeroAsiento(asiento.getNumeroAsiento());
        existente.setFilaAsiento(asiento.getFilaAsiento());
        if (asiento.getSala() != null && asiento.getSala().getId() != null) {
            Sala sala = salaRepository.findById(asiento.getSala().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada con id " + asiento.getSala().getId()));
            existente.setSala(sala);
        }
        return asientoRepository.save(existente);
    }

    @Override
    public void eliminarAsiento(Integer id) {
        if (!asientoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Asiento no encontrado con id " + id);
        }
        asientoRepository.deleteById(id);
    }
}
