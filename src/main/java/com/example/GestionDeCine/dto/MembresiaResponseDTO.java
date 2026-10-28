package com.example.GestionDeCine.dto;

import com.example.GestionDeCine.model.TipoMembresia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Membresia tal como la devuelve la API. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembresiaResponseDTO {
    private Integer id;
    private Integer usuarioId;
    private int puntos;
    private TipoMembresia tipoMembresia;
}
