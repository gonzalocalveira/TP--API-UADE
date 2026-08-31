package com.example.GestionDeCine.model.Interface;

public interface ICliente {
    List<Entrada> verHistorial();
    String verMembresia();
    void comprarEntrada();
    List<Pelicula> verCartelera();
}
