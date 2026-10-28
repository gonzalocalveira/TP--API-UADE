package com.example.GestionDeCine.mapper;

import com.example.GestionDeCine.dto.AsientoResponseDTO;
import com.example.GestionDeCine.dto.ButacaDTO;
import com.example.GestionDeCine.model.Asiento;

/** Convierte Asiento (entidad) a sus DTOs de respuesta. */
public final class AsientoMapper {

    private AsientoMapper() {
    }

    // getSala().getId() no dispara una consulta: el id ya esta en el proxy LAZY.
    public static AsientoResponseDTO toResponse(Asiento asiento) {
        return new AsientoResponseDTO(
                asiento.getId(),
                asiento.getNumeroAsiento(),
                asiento.getFilaAsiento(),
                asiento.getSala().getId());
    }

    public static ButacaDTO toButaca(Asiento asiento, boolean ocupada) {
        return new ButacaDTO(asiento.getId(), asiento.getFilaAsiento(), asiento.getNumeroAsiento(), ocupada);
    }
}
