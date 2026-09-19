package com.example.GestionDeCine.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.*;
@Entity
@Data
public class Asiento {
    @Id
    @GeneratedValue (strategy= GenerationType.IDENTITY)

    private Integer id;
    private int numeroAsiento;
    private String filaAsiento;
    @OneToOne
    @JoinColumn(name="sala_id")
    private  Sala sala;


}
