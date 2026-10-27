package com.example.GestionDeCine.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Pelicula tal como la devuelve la API. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeliculaResponseDTO {
    private Integer id;
    private String nombre;
    private String director;
    private Integer duracion;
    private LocalDate fechaEstreno;
    private String idioma;
    private String genero;
    private String clasificacionEdad;
    private String sinopsis;
}
