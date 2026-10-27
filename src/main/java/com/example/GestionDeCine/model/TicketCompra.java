package com.example.GestionDeCine.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Compra de un usuario para una funcion: agrupa una o mas entradas (una por
 * butaca) y guarda el total calculado por el servidor.
 */
@Entity
@Table(name = "ticket_compra")
@Getter
@Setter
@NoArgsConstructor
public class TicketCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoCompra estado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcion_id", nullable = false)
    private Funcion funcion;

    // mappedBy: la FK vive en Entrada.ticketCompra. Cascade ALL porque las entradas
    // nacen con la compra y no existen sin ella: guardar el ticket guarda sus entradas.
    @OneToMany(mappedBy = "ticketCompra", cascade = CascadeType.ALL)
    private List<Entrada> entradas = new ArrayList<>();

    /** Agrega una entrada manteniendo sincronizados los dos lados de la relacion. */
    public void agregarEntrada(Entrada entrada) {
        entrada.setTicketCompra(this);
        entradas.add(entrada);
    }
}
