package com.example.GestionDeCine.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
public class Entrada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)


    private Integer id;
    private  String estado;
    private  Funcion funcion;
    private Asiento asiento;

    public Entrada(Integer id,
                   String estado, Funcion funcion, Asiento asiento) {
        this.id = id;
        this.estado = estado;
        this.funcion = funcion;
        this.asiento = asiento;
    }
    public Entrada(){}
}
