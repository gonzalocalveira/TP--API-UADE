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
    // Lombok genera isSocio() para este boolean y Jackson lo llamaria "socio".
    // @JsonProperty lo renombra a "isSocio" para mantener el nombre que ya usaba la API.
    @JsonProperty("isSocio")
    private boolean socio;
    private Rol rol;
}
