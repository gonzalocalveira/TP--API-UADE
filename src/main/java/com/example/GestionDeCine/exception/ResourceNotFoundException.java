package com.example.GestionDeCine.exception;

/**
 * Se lanza cuando se busca una entidad por id (u otra clave) y no existe.
 * El GlobalExceptionHandler la traduce a HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
