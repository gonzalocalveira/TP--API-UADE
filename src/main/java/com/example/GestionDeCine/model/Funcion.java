package com.example.GestionDeCine.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Una funcion es la unidad "vendible" del cine: una Pelicula proyectada en
 * una Sala, en una fecha/horario y formato determinados, con un precio de
 * entrada. Las relaciones con Pelicula y Sala son unidireccionales
 * (@ManyToOne desde Funcion) para evitar colecciones bidireccionales que
 * compliquen la serializacion JSON y el equals/hashCode de Lombok.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Funcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "La fecha de la funcion es obligatoria")
    private LocalDate fechaFuncion;

    @NotNull(message = "El horario de la funcion es obligatorio")
    private LocalTime horaFuncion;

    @NotNull(message = "El formato de la funcion es obligatorio")
    @Enumerated(EnumType.STRING)
    private FormatoFuncion formatoFuncion;

    @NotNull(message = "El precio de la entrada es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private Double precio;

    @NotNull(message = "La funcion debe tener una pelicula asociada")
    @ManyToOne
    @JoinColumn(name = "pelicula_id", nullable = false)
    private Pelicula pelicula;

    @NotNull(message = "La funcion debe tener una sala asociada")
    @ManyToOne
    @JoinColumn(name = "sala_id", nullable = false)
    private Sala sala;
}
