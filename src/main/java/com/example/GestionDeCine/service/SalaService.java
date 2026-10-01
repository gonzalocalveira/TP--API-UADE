package com.example.GestionDeCine.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.model.Sala;
import com.example.GestionDeCine.repository.SalaRepository;
import com.example.GestionDeCine.service.interfaces.ISalaService;

@Service
public class SalaService implements ISalaService {

    private final SalaRepository salaRepository;

    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    @Override
    public List<Sala> obtenerSalas() {
        return salaRepository.findAll();
    }

    @Override
    public Sala obtenerSalaPorId(Integer id) {
        return salaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada con id " + id));
    }

    @Override
    public Sala crearSala(Sala sala) {
        return salaRepository.save(sala);
    }

    @Override
    public Sala modificarSala(Integer id, Sala sala) {
        Sala existente = obtenerSalaPorId(id);
        existente.setNombre(sala.getNombre());
        existente.setCapacidad(sala.getCapacidad());
        return salaRepository.save(existente);
    }

    @Override
    public void eliminarSala(Integer id) {
        if (!salaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sala no encontrada con id " + id);
        }
        salaRepository.deleteById(id);
    }
}
