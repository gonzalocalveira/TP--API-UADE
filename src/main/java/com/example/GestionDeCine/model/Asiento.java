package com.example.GestionDeCine.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Un asiento fisico dentro de una {@link Sala}. La relacion con Sala es
 * @ManyToOne (muchos asientos por sala) y no @OneToOne como estaba en el
 * diseño original, que solo permitia un asiento por sala.
 */
@Entity
// Nota: los nombres de columna van en snake_case (sala_id, fila_asiento,
// numero_asiento) porque esa es la convencion que usa por defecto la
// naming strategy de Spring Boot/Hibernate para convertir los nombres de
// campo (camelCase) a nombres de columna SQL.
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "sala_id", "fila_asiento", "numero_asiento" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "El numero de asiento es obligatorio")
    @Positive(message = "El numero de asiento debe ser mayor a 0")
    private Integer numeroAsiento;

    @NotBlank(message = "La fila es obligatoria")
    private String filaAsiento;

    @NotNull(message = "El asiento debe pertenecer a una sala")
    @ManyToOne
    @JoinColumn(name = "sala_id", nullable = false)
    private Sala sala;
}
