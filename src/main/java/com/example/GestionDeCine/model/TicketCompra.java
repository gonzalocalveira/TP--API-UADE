package com.example.GestionDeCine.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Comprobante de compra: registra que un {@link Usuario} pago un
 * {@link Entrada} en un momento (fechaCompra) y a un precio determinado
 * (se copia el precio de la Funcion al momento de la compra, para que un
 * cambio de precio futuro no altere el historial de compras ya realizadas).
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private LocalDateTime fechaCompra;

    @NotNull
    @Positive(message = "El precio debe ser mayor a 0")
    private Double precio;

    @NotNull(message = "El ticket debe estar asociado a un usuario")
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull(message = "El ticket debe estar asociado a una entrada")
    @OneToOne
    @JoinColumn(name = "entrada_id", nullable = false, unique = true)
    private Entrada entrada;
}
