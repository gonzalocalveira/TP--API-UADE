package com.example.GestionDeCine.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Catalogo de peliculas que el cine puede proyectar. */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pelicula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El director es obligatorio")
    private String director;

    @NotNull(message = "La duracion es obligatoria")
    @Positive(message = "La duracion (en minutos) debe ser mayor a 0")
    private Integer duracion;

    @NotNull(message = "La fecha de estreno es obligatoria")
    private LocalDate fechaEstreno;

    @NotBlank(message = "El idioma es obligatorio")
    private String idioma;

    @NotBlank(message = "El genero es obligatorio")
    private String genero;

    @NotBlank(message = "La clasificacion por edad es obligatoria")
    private String clasificacionEdad;

    private String sinopsis;
}
