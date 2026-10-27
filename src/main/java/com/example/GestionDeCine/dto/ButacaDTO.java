package com.example.GestionDeCine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Una butaca dentro del mapa de una funcion: indica si esta ocupada para esa funcion. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ButacaDTO {
    private Integer id;
    private String filaAsiento;
    private Integer numeroAsiento;
    private boolean ocupada;
}
