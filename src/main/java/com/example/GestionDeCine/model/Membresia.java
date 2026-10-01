package com.example.GestionDeCine.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Membresia/socio del cine. Relacion 1 a 1 con Usuario: cada Membresia
 * pertenece a un unico Usuario y cada Usuario tiene, a lo sumo, una
 * Membresia. Esta clase es el lado "dueño" de la relacion (tiene la
 * foreign key usuario_id, con restriccion unique para que no puedan existir
 * dos membresias para el mismo usuario).
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Membresia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "La membresia debe estar asociada a un usuario")
    @OneToOne
    @JoinColumn(name = "usuario_id", unique = true, nullable = false)
    private Usuario usuario;

    @PositiveOrZero(message = "Los puntos no pueden ser negativos")
    private int puntos;

    @NotNull(message = "El tipo de membresia es obligatorio")
    @Enumerated(EnumType.STRING)
    private TipoMembresia tipoMembresia;
}
