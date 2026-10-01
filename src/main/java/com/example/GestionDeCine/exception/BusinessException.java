package com.example.GestionDeCine.exception;

/**
 * Se lanza cuando una operacion es sintacticamente valida (los datos tienen
 * el formato correcto) pero viola una regla de negocio: por ejemplo, un
 * asiento que ya esta ocupado, un mail que ya esta registrado, una
 * password incorrecta al iniciar sesion, etc.
 * El GlobalExceptionHandler la traduce a HTTP 409 Conflict.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
