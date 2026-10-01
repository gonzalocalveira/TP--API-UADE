package com.example.GestionDeCine.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO de entrada para POST /api/compras. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraRequestDTO {

    @NotNull(message = "El id de usuario es obligatorio")
    private Integer usuarioId;

    @NotNull(message = "El id de funcion es obligatorio")
    private Integer funcionId;

    @NotNull(message = "El id de asiento es obligatorio")
    private Integer asientoId;
}
