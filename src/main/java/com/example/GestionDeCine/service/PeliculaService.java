package com.example.GestionDeCine.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GestionDeCine.dto.PeliculaRequestDTO;
import com.example.GestionDeCine.dto.PeliculaResponseDTO;
import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.mapper.PeliculaMapper;
import com.example.GestionDeCine.model.Pelicula;
import com.example.GestionDeCine.repository.FuncionRepository;
import com.example.GestionDeCine.repository.PeliculaRepository;
import com.example.GestionDeCine.service.interfaces.IPeliculaService;

/** Logica de negocio del catalogo de peliculas. */
@Service
public class PeliculaService implements IPeliculaService {

    private final PeliculaRepository peliculaRepository;
    private final FuncionRepository funcionRepository;

    public PeliculaService(PeliculaRepository peliculaRepository, FuncionRepository funcionRepository) {
        this.peliculaRepository = peliculaRepository;
        this.funcionRepository = funcionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeliculaResponseDTO> obtenerPeliculas() {
        return peliculaRepository.findAll().stream().map(PeliculaMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PeliculaResponseDTO obtenerPeliculaPorId(Integer id) {
        return PeliculaMapper.toResponse(buscarPelicula(id));
    }

    @Override
    @Transactional
    public PeliculaResponseDTO crearPelicula(PeliculaRequestDTO dto) {
        Pelicula pelicula = new Pelicula();
        PeliculaMapper.copiarDatos(dto, pelicula);
        return PeliculaMapper.toResponse(peliculaRepository.save(pelicula));
    }

    @Override
    @Transactional
    public PeliculaResponseDTO modificarPelicula(Integer id, PeliculaRequestDTO dto) {
        Pelicula pelicula = buscarPelicula(id);
        PeliculaMapper.copiarDatos(dto, pelicula);
        return PeliculaMapper.toResponse(peliculaRepository.save(pelicula));
    }

    /** No se borra una pelicula con funciones: se perderia la historia de lo programado y vendido. */
    @Override
    @Transactional
    public void eliminarPelicula(Integer id) {
        Pelicula pelicula = buscarPelicula(id);
        if (funcionRepository.existsByPeliculaId(id)) {
            throw new BusinessException("No se puede eliminar la pelicula " + id + " porque tiene funciones asociadas");
        }
        peliculaRepository.delete(pelicula);
    }

    private Pelicula buscarPelicula(Integer id) {
        return peliculaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la pelicula con id " + id));
    }
}
