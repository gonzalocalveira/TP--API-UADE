package com.example.GestionDeCine.model;

/**
 * Estado de una {@link Entrada} (un asiento asociado a una funcion puntual).
 * Se agrega respecto del diseño original (que usaba un String libre para
 * "estado") para evitar valores invalidos como "ocupadoo" o "OCUPADO " y
 * poder comparar con == / switch de forma segura.
 */
public enum EstadoEntrada {
    DISPONIBLE,
    OCUPADA,
    CANCELADA
}
