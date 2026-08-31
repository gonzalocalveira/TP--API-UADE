package com.example.GestionDeCine.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.*;

import com.example.GestionDeCine.model.Interface.ICliente;

@Entity
@Data
@ToString
public class Cliente implements ICliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private boolean isSocio;
    private String nombreApellido;
    private int dni;
    private LocalDate fechaNacimiento;
    private String mail;
    private String password;

    public Cliente() {
    }

    public Cliente(String password,
                   String mail,
                   LocalDate fechaNacimiento,
                   int dni,
                   String nombreApellido,
                   boolean isSocio) {

        this.password = password;
        this.mail = mail;
        this.fechaNacimiento = fechaNacimiento;
        this.dni = dni;
        this.nombreApellido = nombreApellido;
        this.isSocio = isSocio;
    }

    @Override
    public List<Entrada> verHistorial() {
        return List.of();
    }

    @Override
    public String verMembresia() {
        return "";
    }

    @Override
    public void comprarEntrada() {
    }

    @Override
    public List<Pelicula> verCartelera() {
        return List.of();
    }
}
