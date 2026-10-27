package com.example.GestionDeCine.mapper;

import com.example.GestionDeCine.dto.MembresiaResponseDTO;
import com.example.GestionDeCine.model.Membresia;

/** Convierte Membresia (entidad) a su DTO de respuesta. */
public final class MembresiaMapper {

    private MembresiaMapper() {
    }

    public static MembresiaResponseDTO toResponse(Membresia membresia) {
        return new MembresiaResponseDTO(
                membresia.getId(),
                membresia.getUsuario().getId(),
                membresia.getPuntos(),
                membresia.getTipoMembresia());
    }
}
