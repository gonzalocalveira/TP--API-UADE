package com.example.GestionDeCine.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@ToString
@Table (name="usuarios")
public class Usuario{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private boolean isSocio;
    private String nombreApellido;
    private int dni;
    private LocalDate fechaNacimiento;
    private String mail;
    private String password;
    @Enumerated(EnumType.STRING)
    private Rol rol;
    @Enumerated(EnumType.STRING)
    private Membresia membresia;

    public Usuario() {
    }

    public Usuario(String password,
                   String mail,
                   LocalDate fechaNacimiento,
                   int dni,
                   String nombreApellido,
                   boolean isSocio,
                   Rol rol,
                   Membresia membresia) {

        this.password = password;
        this.mail = mail;
        this.fechaNacimiento = fechaNacimiento;
        this.dni = dni;
        this.nombreApellido = nombreApellido;
        this.isSocio = isSocio;
        this.rol=rol;
        this.membresia=membresia;
    }
}
