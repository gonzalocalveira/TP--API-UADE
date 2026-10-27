package com.example.GestionDeCine.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Datos que envia el cliente para crear o modificar una pelicula (POST/PUT /api/peliculas). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeliculaRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre admite hasta 150 caracteres")
    private String nombre;

    @NotBlank(message = "El director es obligatorio")
    @Size(max = 100, message = "El director admite hasta 100 caracteres")
    private String director;

    @NotNull(message = "La duracion es obligatoria")
    @Positive(message = "La duracion (en minutos) debe ser mayor a 0")
    private Integer duracion;

    @NotNull(message = "La fecha de estreno es obligatoria")
    private LocalDate fechaEstreno;

    @NotBlank(message = "El idioma es obligatorio")
    @Size(max = 50, message = "El idioma admite hasta 50 caracteres")
    private String idioma;

    @NotBlank(message = "El genero es obligatorio")
    @Size(max = 50, message = "El genero admite hasta 50 caracteres")
    private String genero;

    @NotBlank(message = "La clasificacion por edad es obligatoria")
    @Size(max = 20, message = "La clasificacion admite hasta 20 caracteres")
    private String clasificacionEdad;

    @NotBlank(message = "La sinopsis es obligatoria")
    @Size(max = 1000, message = "La sinopsis admite hasta 1000 caracteres")
    private String sinopsis;
}
