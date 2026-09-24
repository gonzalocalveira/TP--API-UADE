package com.example.GestionDeCine.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Data

public class Pelicula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
