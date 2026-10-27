package com.example.GestionDeCine.mapper;

import com.example.GestionDeCine.dto.SalaRequestDTO;
import com.example.GestionDeCine.dto.SalaResponseDTO;
import com.example.GestionDeCine.model.Sala;

/** Convierte Sala (entidad) a y desde sus DTOs. */
public final class SalaMapper {

    private SalaMapper() {
    }

    public static SalaResponseDTO toResponse(Sala sala) {
        return new SalaResponseDTO(sala.getId(), sala.getNombre(), sala.getCapacidad());
    }

    public static void copiarDatos(SalaRequestDTO dto, Sala sala) {
        sala.setNombre(dto.getNombre().trim());
        sala.setCapacidad(dto.getCapacidad());
    }
}
