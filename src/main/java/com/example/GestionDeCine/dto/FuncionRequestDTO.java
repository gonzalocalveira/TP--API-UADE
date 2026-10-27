package com.example.GestionDeCine.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.example.GestionDeCine.model.FormatoFuncion;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Datos para crear o modificar una funcion (POST/PUT /api/funciones). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuncionRequestDTO {

    @NotNull(message = "La fecha de la funcion es obligatoria")
    @FutureOrPresent(message = "La fecha de la funcion no puede ser pasada")
    private LocalDate fechaFuncion;

    @NotNull(message = "El horario de la funcion es obligatorio")
    private LocalTime horaFuncion;

    @NotNull(message = "El formato es obligatorio (DOS_D, TRES_D o CUATRO_DX)")
    private FormatoFuncion formatoFuncion;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 8 enteros y 2 decimales")
    private BigDecimal precio;

    @NotNull(message = "El id de la pelicula es obligatorio")
    private Integer peliculaId;

    @NotNull(message = "El id de la sala es obligatorio")
    private Integer salaId;
}
