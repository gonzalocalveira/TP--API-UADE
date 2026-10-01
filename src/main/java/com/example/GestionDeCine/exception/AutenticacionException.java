package com.example.GestionDeCine.exception;

/**
 * Se lanza cuando el login falla (mail no registrado o password incorrecta).
 * Se maneja aparte de BusinessException para poder devolver 401 Unauthorized
 * en vez de 409 Conflict.
 */
public class AutenticacionException extends RuntimeException {
    public AutenticacionException(String message) {
        super(message);
    }
}
