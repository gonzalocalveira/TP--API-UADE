package com.example.GestionDeCine.service.interfaces;

import java.util.List;

import com.example.GestionDeCine.dto.PeliculaRequestDTO;
import com.example.GestionDeCine.dto.PeliculaResponseDTO;

/** Contrato de la logica de negocio de peliculas. Los controllers dependen de esta interfaz, no de la implementacion. */
public interface IPeliculaService {

    List<PeliculaResponseDTO> obtenerPeliculas();

    PeliculaResponseDTO obtenerPeliculaPorId(Integer id);

    PeliculaResponseDTO crearPelicula(PeliculaRequestDTO dto);

    PeliculaResponseDTO modificarPelicula(Integer id, PeliculaRequestDTO dto);

    /** Falla con 409 si la pelicula tiene funciones. */
    void eliminarPelicula(Integer id);
}
