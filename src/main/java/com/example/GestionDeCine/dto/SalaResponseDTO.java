package com.example.GestionDeCine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Sala tal como la devuelve la API. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaResponseDTO {
    private Integer id;
    private String nombre;
    private Integer capacidad;
}
