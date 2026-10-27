package com.example.GestionDeCine.dto;

import com.example.GestionDeCine.model.TipoMembresia;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Datos para dar de alta una membresia (POST /api/membresias). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembresiaRequestDTO {

    @NotNull(message = "El id de usuario es obligatorio")
    private Integer usuarioId;

    @PositiveOrZero(message = "Los puntos no pueden ser negativos")
    private int puntos;

    @NotNull(message = "El tipo de membresia es obligatorio (CLASSIC, GOLD o BLACK)")
    private TipoMembresia tipoMembresia;
}
