package com.example.GestionDeCine.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "El estado de la entrada es obligatorio")
    @Enumerated(EnumType.STRING)
    private EstadoEntrada estado;

    @NotNull(message = "La entrada debe estar asociada a una funcion")
    @ManyToOne
    @JoinColumn(name = "funcion_id", nullable = false)
    private Funcion funcion;

    @NotNull(message = "La entrada debe estar asociada a un asiento")
    @ManyToOne
    @JoinColumn(name = "asiento_id", nullable = false)
    private Asiento asiento;
}
