package com.example.GestionDeCine.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Persona con acceso al sistema (cliente, empleado o administrador).
 * Mapea a la tabla "usuarios". Nunca se devuelve al cliente HTTP: se usa
 * UsuarioResponseDTO, que no incluye la password.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre_apellido", nullable = false, length = 100)
    private String nombreApellido;

    @Column(name = "dni", nullable = false, unique = true)
    private Integer dni;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "mail", nullable = false, unique = true, length = 100)
    private String mail;

    // Se guarda en texto plano porque la Etapa 1 no incluye Spring Security; con seguridad se guardaria un hash BCrypt.
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    /** true si el usuario tiene una Membresia. Lo actualiza MembresiaService. */
    @Column(name = "is_socio", nullable = false)
    private boolean isSocio;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private Rol rol;
}
