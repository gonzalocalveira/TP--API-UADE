package com.example.GestionDeCine.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.GestionDeCine.dto.ErrorResponseDTO;

/**
 * Traduce cualquier excepcion que salga de un controller a una respuesta HTTP
 * con el codigo correcto y siempre el mismo formato JSON (ErrorResponseDTO).
 * Asi ningun controller necesita try/catch y el front maneja todos los errores igual.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 404: se pidio un recurso por id que no existe. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    /** 409: una regla de negocio impide la operacion (butaca ocupada, mail repetido, funcion superpuesta...). */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusiness(BusinessException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    /** 401: login con mail o password incorrectos. */
    @ExceptionHandler(AutenticacionException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuth(AutenticacionException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null);
    }

    /** 400: @Valid encontro campos invalidos en el body. Se devuelve el detalle campo por campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Datos de entrada invalidos", request, detalles);
    }

    /** 400: el body no es JSON valido o un valor no se puede convertir (fecha mal escrita, enum inexistente). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleBodyIlegible(HttpMessageNotReadableException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "El body del request no es un JSON valido o tiene valores con formato incorrecto", request, null);
    }

    /** 400: un parametro de la URL tiene el tipo equivocado (ej: /api/peliculas/abc o ?fecha=ayer). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTipoIncorrecto(MethodArgumentTypeMismatchException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "El parametro '" + ex.getName() + "' tiene un valor invalido: " + ex.getValue(), request, null);
    }

    /** 400: falta un parametro obligatorio de la URL. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDTO> handleParametroFaltante(MissingServletRequestParameterException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Falta el parametro obligatorio '" + ex.getParameterName() + "'", request, null);
    }

    /** 404: la ruta pedida no existe (ej: /api/pelis). */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleRutaInexistente(NoResourceFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, "No existe el endpoint solicitado", request, null);
    }

    /** 405: el endpoint existe pero no acepta ese metodo HTTP. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request, null);
    }

    /**
     * 409: la base rechazo la operacion por una restriccion (UNIQUE, FOREIGN KEY, CHECK).
     * Es la ultima red de seguridad: los services ya chequean estos casos antes,
     * pero dos requests simultaneos podrian pasar ese chequeo.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleIntegridad(DataIntegrityViolationException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT,
                "La operacion viola una restriccion de la base de datos (dato repetido o registro con dependencias)",
                request, null);
    }

    /** 500: error no previsto. Se registra completo en el log del servidor, pero al cliente no se le filtra el detalle. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex, WebRequest request) {
        log.error("Error inesperado en {}", request.getDescription(false), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado en el servidor", request, null);
    }

    private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String message, WebRequest request, List<String> detalles) {
        ErrorResponseDTO body = new ErrorResponseDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getDescription(false).replace("uri=", ""),
                detalles);
        return ResponseEntity.status(status).body(body);
    }
}
