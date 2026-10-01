package com.example.GestionDeCine.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sala fisica del cine. En el diseño original, Sala tenia una lista de
 * Peliculas, lo cual no representa el dominio correctamente: una sala no
 * "pertenece" a una pelicula, sino que en una sala se programan Funciones
 * (una Funcion vincula una Pelicula con una Sala en una fecha/horario
 * determinado). Esa relacion se movio a {@link Funcion}.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre de la sala es obligatorio")
    private String nombre;

    @NotNull(message = "La capacidad es obligatoria")
    @Positive(message = "La capacidad debe ser mayor a 0")
    private Integer capacidad;
}
