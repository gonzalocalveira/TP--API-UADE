package com.example.GestionDeCine.dto;

import java.math.BigDecimal;

import com.example.GestionDeCine.model.EstadoEntrada;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Una butaca dentro de una compra. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntradaResponseDTO {
    private Integer id;
    private Integer asientoId;
    private String filaAsiento;
    private Integer numeroAsiento;
    private BigDecimal precioUnitario;
    private EstadoEntrada estado;
}
