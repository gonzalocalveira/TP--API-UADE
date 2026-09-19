package com.example.GestionDeCine.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;

import lombok.*;

@Entity
@Data

public class Pelicula {
    private Integer id;
    private String nombre;
    private String director;
    private int duracion;
    private LocalDate fechaEstreno;
    private String idioma;
    private String genero;
    private String clasificacionEdad;
    private String sinopsis;

    public Pelicula(Integer id, String nombre,
                    String director, int duracion,
                    LocalDate fechaEstreno,
                    String idioma, String genero,
                    String clasificacionEdad,
                    String sinopsis) {
        this.id = id;
        this.nombre = nombre;
        this.director = director;
        this.duracion = duracion;
        this.fechaEstreno = fechaEstreno;
        this.idioma = idioma;
        this.genero = genero;
        this.clasificacionEdad = clasificacionEdad;
        this.sinopsis = sinopsis;
    }
}
