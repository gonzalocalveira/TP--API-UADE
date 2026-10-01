package com.example.GestionDeCine.model;

/**
 * Roles posibles de un {@link Usuario} dentro del sistema.
 * Se persiste como STRING (ver @Enumerated en Usuario) para que la base de
 * datos sea legible directamente ("CLIENTE" en vez de un indice numerico
 * que se rompe si se reordena el enum).
 */
public enum Rol {
    CLIENTE,
    EMPLEADO,
    ADMINISTRADOR
}
