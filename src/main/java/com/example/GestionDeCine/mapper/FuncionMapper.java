package com.example.GestionDeCine.mapper;

import com.example.GestionDeCine.dto.FuncionResponseDTO;
import com.example.GestionDeCine.model.Funcion;

/** Convierte Funcion (entidad) a su DTO de respuesta. */
public final class FuncionMapper {

    private FuncionMapper() {
    }

    public static FuncionResponseDTO toResponse(Funcion funcion) {
        return new FuncionResponseDTO(
                funcion.getId(),
                funcion.getFechaFuncion(),
                funcion.getHoraFuncion(),
                funcion.getFormatoFuncion(),
                funcion.getPrecio(),
                funcion.getPelicula().getId(),
                funcion.getPelicula().getNombre(),
                funcion.getPelicula().getDuracion(),
                funcion.getSala().getId(),
                funcion.getSala().getNombre());
    }
}
