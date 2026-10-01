package com.example.GestionDeCine.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO de entrada para POST /api/usuarios/login. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "El mail es obligatorio")
    @Email(message = "El mail no tiene un formato valido")
    private String mail;

    @NotBlank(message = "La password es obligatoria")
    private String password;
}
