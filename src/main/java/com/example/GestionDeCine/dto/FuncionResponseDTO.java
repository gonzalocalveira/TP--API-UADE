package com.example.GestionDeCine.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.example.GestionDeCine.model.FormatoFuncion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Funcion tal como la devuelve la API, con los datos de pelicula y sala que necesita mostrar un cliente. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuncionResponseDTO {
    private Integer id;
    private LocalDate fechaFuncion;
    private LocalTime horaFuncion;
    private FormatoFuncion formatoFuncion;
    private BigDecimal precio;
    private Integer peliculaId;
    private String peliculaNombre;
    private Integer duracion;
    private Integer salaId;
    private String salaNombre;
}
