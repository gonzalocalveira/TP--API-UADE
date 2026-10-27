package com.example.GestionDeCine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Datos para crear o modificar una sala (POST/PUT /api/salas). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaRequestDTO {

    @NotBlank(message = "El nombre de la sala es obligatorio")
    @Size(max = 50, message = "El nombre admite hasta 50 caracteres")
    private String nombre;

    @NotNull(message = "La capacidad es obligatoria")
    @Positive(message = "La capacidad debe ser mayor a 0")
    private Integer capacidad;
}
