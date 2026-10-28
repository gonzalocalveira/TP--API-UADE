package com.example.GestionDeCine.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Proyeccion puntual: una pelicula, en una sala, en una fecha y hora, a un precio. */
@Entity
@Table(name = "funcion")
@Getter
@Setter
@NoArgsConstructor
public class Funcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "fecha_funcion", nullable = false)
    private LocalDate fechaFuncion;

    @Column(name = "hora_funcion", nullable = false)
    private LocalTime horaFuncion;

    @Enumerated(EnumType.STRING)
    @Column(name = "formato_funcion", nullable = false)
    private FormatoFuncion formatoFuncion;

    // BigDecimal y no Double: el dinero no debe tener errores de redondeo.
    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pelicula_id", nullable = false)
    private Pelicula pelicula;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sala_id", nullable = false)
    private Sala sala;

    /** Fecha y hora de inicio combinadas, para comparar contra "ahora" y entre funciones. */
    public LocalDateTime getInicio() {
        return LocalDateTime.of(fechaFuncion, horaFuncion);
    }
}
