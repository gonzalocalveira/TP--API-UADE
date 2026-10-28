package com.example.GestionDeCine.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.example.GestionDeCine.model.EstadoCompra;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Compra tal como la devuelve la API (comprobante con sus entradas). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCompraResponseDTO {
    private Integer id;
    private LocalDateTime fechaCompra;
    private EstadoCompra estado;
    private BigDecimal total;
    private Integer usuarioId;
    private Integer funcionId;
    private String peliculaNombre;
    private String salaNombre;
    private LocalDate fechaFuncion;
    private LocalTime horaFuncion;
    private List<EntradaResponseDTO> entradas;
}
