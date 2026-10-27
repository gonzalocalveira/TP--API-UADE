package com.example.GestionDeCine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Butaca tal como la devuelve la API. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsientoResponseDTO {
    private Integer id;
    private Integer numeroAsiento;
    private String filaAsiento;
    private Integer salaId;
}
