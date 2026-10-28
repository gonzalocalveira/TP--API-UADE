package com.example.GestionDeCine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Datos para crear o modificar una butaca (POST/PUT /api/asientos). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsientoRequestDTO {

    @NotNull(message = "El numero de asiento es obligatorio")
    @Positive(message = "El numero de asiento debe ser mayor a 0")
    private Integer numeroAsiento;

    @NotBlank(message = "La fila es obligatoria")
    @Size(max = 5, message = "La fila admite hasta 5 caracteres")
    private String filaAsiento;

    @NotNull(message = "El id de la sala es obligatorio")
    private Integer salaId;
}
