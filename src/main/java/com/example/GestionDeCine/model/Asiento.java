package com.example.GestionDeCine.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Butaca fisica dentro de una sala (fila + numero). Mapea a la tabla "asiento". */
@Entity
@Table(name = "asiento",
        uniqueConstraints = @UniqueConstraint(name = "uk_asiento_ubicacion",
                columnNames = { "sala_id", "fila_asiento", "numero_asiento" }))
@Getter
@Setter
@NoArgsConstructor
public class Asiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_asiento", nullable = false)
    private Integer numeroAsiento;

    @Column(name = "fila_asiento", nullable = false, length = 5)
    private String filaAsiento;

    // Muchos asientos pertenecen a una sala. LAZY: la sala se carga solo si se usa.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sala_id", nullable = false)
    private Sala sala;
}
