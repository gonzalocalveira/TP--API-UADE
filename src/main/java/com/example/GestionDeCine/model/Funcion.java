package com.example.GestionDeCine.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
public class Funcion {
    @id
    @GeneratedValue (strategy = GenerationType.IDENTITY)

    private Integer id;
    private LocalDate fechaFuncion;
    private FormatoFuncion formatoFuncion;
    private Pelicula pelicula;





}
