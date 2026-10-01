package com.example.GestionDeCine.dto;

import java.time.LocalDate;

import com.example.GestionDeCine.model.Rol;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de salida para Usuario. Existe para no devolver nunca la password
 * (ni siquiera hasheada) en las respuestas de la API: la entidad Usuario
 * tiene ese campo porque lo necesita para persistir/validar el login, pero
 * el cliente HTTP nunca deberia recibirlo de vuelta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    private Integer id;
    private String nombreApellido;
    private Integer dni;
    private LocalDate fechaNacimiento;
    private String mail;
    // Con un boolean llamado "isSocio", Lombok genera isSocio()/setSocio() y
    // Jackson lo expondria como "socio". @JsonProperty fuerza el nombre
    // "isSocio" en el JSON (tanto al leer el body como al responder).
    @JsonProperty("isSocio")
    private boolean isSocio;
    private Rol rol;
}
