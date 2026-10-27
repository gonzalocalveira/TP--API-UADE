package com.example.GestionDeCine.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Datos para registrar o modificar un usuario (POST/PUT /api/usuarios).
 * No incluye rol ni isSocio: el registro publico siempre crea un CLIENTE y la
 * condicion de socio la maneja MembresiaService. Si el cliente los manda, se ignoran.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {

    @NotBlank(message = "El nombre y apellido es obligatorio")
    @Size(max = 100, message = "El nombre admite hasta 100 caracteres")
    private String nombreApellido;

    @NotNull(message = "El DNI es obligatorio")
    @Positive(message = "El DNI debe ser un numero positivo")
    private Integer dni;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El mail es obligatorio")
    @Email(message = "El mail no tiene un formato valido")
    @Size(max = 100, message = "El mail admite hasta 100 caracteres")
    private String mail;

    @NotBlank(message = "La password es obligatoria")
    @Size(min = 4, max = 100, message = "La password debe tener entre 4 y 100 caracteres")
    private String password;
}
