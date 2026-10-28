package com.example.GestionDeCine.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pedido de compra (POST /api/clientes/compras): quien compra, para que funcion
 * y que butacas. No trae precio ni total: los calcula el servidor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraRequestDTO {

    @NotNull(message = "El id de usuario es obligatorio")
    private Integer usuarioId;

    @NotNull(message = "El id de funcion es obligatorio")
    private Integer funcionId;

    @NotEmpty(message = "Hay que elegir al menos una butaca")
    private List<@NotNull(message = "El id de butaca no puede ser nulo") Integer> asientoIds;
}
