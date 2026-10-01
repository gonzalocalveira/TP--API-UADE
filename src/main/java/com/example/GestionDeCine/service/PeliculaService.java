package com.example.GestionDeCine.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.model.Pelicula;
import com.example.GestionDeCine.repository.PeliculaRepository;
import com.example.GestionDeCine.service.interfaces.IPeliculaService;

@Service
public class PeliculaService implements IPeliculaService {

    private final PeliculaRepository peliculaRepository;

    public PeliculaService(PeliculaRepository peliculaRepository) {
        this.peliculaRepository = peliculaRepository;
    }

    @Override
    public List<Pelicula> obtenerPeliculas() {
        return peliculaRepository.findAll();
    }

    @Override
    public Pelicula obtenerPeliculaPorId(Integer id) {
        return peliculaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pelicula no encontrada con id " + id));
    }

    @Override
    public Pelicula crearPelicula(Pelicula pelicula) {
        return peliculaRepository.save(pelicula);
    }

    @Override
    public Pelicula modificarPelicula(Integer id, Pelicula pelicula) {
        Pelicula existente = obtenerPeliculaPorId(id);

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

    @Override
    public void eliminarPelicula(Integer id) {
        if (!peliculaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pelicula no encontrada con id " + id);
        }
        peliculaRepository.deleteById(id);
    }
}
