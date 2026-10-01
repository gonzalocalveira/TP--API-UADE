package com.example.GestionDeCine.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(columnNames = "mail"),
        @UniqueConstraint(columnNames = "dni")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre y apellido es obligatorio")
    private String nombreApellido;

    @NotNull(message = "El DNI es obligatorio")
    @Positive(message = "El DNI debe ser un numero positivo")
    private Integer dni;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El mail es obligatorio")
    @Email(message = "El mail no tiene un formato valido")
    private String mail;

    @NotBlank(message = "La password es obligatoria")
    @Size(min = 4, message = "La password debe tener al menos 4 caracteres")
    // WRITE_ONLY: la password se acepta en el body de los requests (alta,
    // modificacion) pero nunca se escribe en un JSON de respuesta, aunque el
    // Usuario viaje anidado dentro de otra entidad (ej: TicketCompra).
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    // Con un boolean llamado "isSocio", Lombok genera isSocio()/setSocio() y
    // Jackson lo expondria como "socio". @JsonProperty fuerza el nombre
    // "isSocio" en el JSON (tanto al leer el body como al responder).
    @JsonProperty("isSocio")
    private boolean isSocio;

    @NotNull(message = "El rol es obligatorio")
    @Enumerated(EnumType.STRING)
    private Rol rol;
}
