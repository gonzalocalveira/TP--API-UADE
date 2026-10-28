package com.example.GestionDeCine.mapper;

import com.example.GestionDeCine.dto.PeliculaRequestDTO;
import com.example.GestionDeCine.dto.PeliculaResponseDTO;
import com.example.GestionDeCine.model.Pelicula;

/** Convierte Pelicula (entidad) a y desde sus DTOs. Metodos estaticos: no tiene estado. */
public final class PeliculaMapper {

    private PeliculaMapper() {
    }

    public static PeliculaResponseDTO toResponse(Pelicula pelicula) {
        return new PeliculaResponseDTO(
                pelicula.getId(),
                pelicula.getNombre(),
                pelicula.getDirector(),
                pelicula.getDuracion(),
                pelicula.getFechaEstreno(),
                pelicula.getIdioma(),
                pelicula.getGenero(),
                pelicula.getClasificacionEdad(),
                pelicula.getSinopsis());
    }

    /** Copia los datos del request sobre la entidad (sirve para crear y para modificar). */
    public static void copiarDatos(PeliculaRequestDTO dto, Pelicula pelicula) {
        pelicula.setNombre(dto.getNombre().trim());
        pelicula.setDirector(dto.getDirector().trim());
        pelicula.setDuracion(dto.getDuracion());
        pelicula.setFechaEstreno(dto.getFechaEstreno());
        pelicula.setIdioma(dto.getIdioma().trim());
        pelicula.setGenero(dto.getGenero().trim());
        pelicula.setClasificacionEdad(dto.getClasificacionEdad().trim());
        pelicula.setSinopsis(dto.getSinopsis().trim());
    }
}
