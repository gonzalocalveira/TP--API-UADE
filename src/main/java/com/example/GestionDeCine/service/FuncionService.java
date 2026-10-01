package com.example.GestionDeCine.service;

import java.util.List;

import org.springframework.stereotype.Service;

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

@Service
public class FuncionService implements IFuncionService {

    private final FuncionRepository funcionRepository;
    private final PeliculaRepository peliculaRepository;
    private final SalaRepository salaRepository;
    private final AsientoRepository asientoRepository;
    private final EntradaRepository entradaRepository;

    public FuncionService(FuncionRepository funcionRepository,
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
    public List<Funcion> obtenerFunciones() {
        return funcionRepository.findAll();
    }

    @Override
    public Funcion obtenerFuncionPorId(Integer id) {
        return funcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcion no encontrada con id " + id));
    }

    @Override
    public Funcion crearFuncion(Funcion funcion) {
        Pelicula pelicula = peliculaRepository.findById(funcion.getPelicula().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pelicula no encontrada con id " + funcion.getPelicula().getId()));
        Sala sala = salaRepository.findById(funcion.getSala().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada con id " + funcion.getSala().getId()));

        funcion.setPelicula(pelicula);
        funcion.setSala(sala);
        return funcionRepository.save(funcion);
    }

    @Override
    public Funcion modificarFuncion(Integer id, Funcion funcion) {
        Funcion existente = obtenerFuncionPorId(id);

        Pelicula pelicula = peliculaRepository.findById(funcion.getPelicula().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pelicula no encontrada con id " + funcion.getPelicula().getId()));
        Sala sala = salaRepository.findById(funcion.getSala().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada con id " + funcion.getSala().getId()));

        existente.setFechaFuncion(funcion.getFechaFuncion());
        existente.setHoraFuncion(funcion.getHoraFuncion());
        existente.setFormatoFuncion(funcion.getFormatoFuncion());
        existente.setPrecio(funcion.getPrecio());
        existente.setPelicula(pelicula);
        existente.setSala(sala);

        return funcionRepository.save(existente);
    }

    @Override
    public void eliminarFuncion(Integer id) {
        if (!funcionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Funcion no encontrada con id " + id);
        }
        funcionRepository.deleteById(id);
    }

    @Override
    public List<Asiento> obtenerAsientosDisponibles(Integer funcionId) {
        Funcion funcion = obtenerFuncionPorId(funcionId);

        List<Asiento> asientosDeLaSala = asientoRepository.findBySalaId(funcion.getSala().getId());

        return asientosDeLaSala.stream()
                .filter(asiento -> !entradaRepository.existsByFuncionIdAndAsientoIdAndEstado(
                        funcionId, asiento.getId(), EstadoEntrada.OCUPADA))
                .toList();
    }
}
