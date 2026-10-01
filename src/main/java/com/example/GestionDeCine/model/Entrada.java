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

/**
 * Representa la ocupacion de un {@link Asiento} puntual para una
 * {@link Funcion} puntual. Se crea en el momento de la compra (ver
 * ClienteService.comprarEntrada) con estado OCUPADA; no se pre-generan
 * entradas para todos los asientos de todas las funciones (eso se calcula
 * "al vuelo" comparando los asientos de la sala contra las entradas ya
 * ocupadas de esa funcion, ver FuncionService.obtenerAsientosDisponibles).
 */
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
