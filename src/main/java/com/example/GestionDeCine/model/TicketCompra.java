package com.example.GestionDeCine.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
public class TicketCompra {
    @id
    @GeneratedValue( strategy= GenerationType.IDENTITY)

    private Integer id;
    private LocalDateTime fechaCompra;
    private float precio;
    private Usuario usuario;
    private  Entrada entrada;

}
