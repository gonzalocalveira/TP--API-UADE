package com.example.GestionDeCine.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.example.GestionDeCine.dto.ErrorResponseDTO;

/**
 * Punto unico de manejo de errores para toda la API.
 *
 * @RestControllerAdvice intercepta las excepciones lanzadas desde cualquier
 * @RestController y las transforma en una respuesta HTTP con el formato
 * ErrorResponseDTO, en vez de dejar que Spring devuelva su pagina de error
 * generica (HTML) o un stacktrace crudo. Esto cumple con el requerimiento
 * de "manejo de errores y respuestas apropiadas" / "codigos de estado HTTP".
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusiness(BusinessException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(AutenticacionException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuth(AutenticacionException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null);
    }

    /** Se dispara cuando @Valid encuentra campos invalidos en el @RequestBody. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Datos de entrada invalidos", request, detalles);
    }

    /** Se dispara si se llama a un endpoint con un metodo HTTP que no soporta (ej: DELETE donde solo hay GET). */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request, null);
    }

    /**
     * Se dispara cuando el body no es un JSON valido o tiene un valor que no se
     * puede convertir (ej: una fecha mal escrita o un enum inexistente como
     * "rol": "SUPERHEROE").
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleBodyIlegible(HttpMessageNotReadableException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "El body del request no es un JSON valido o tiene valores con formato incorrecto", request, null);
    }

    /**
     * Se dispara cuando la base de datos rechaza la operacion por una
     * restriccion SQL: tipicamente al eliminar un registro que todavia esta
     * referenciado por una foreign key (ej: borrar una Sala que tiene Asientos
     * o Funciones), o al violar una restriccion UNIQUE.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleIntegridad(DataIntegrityViolationException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT,
                "La operacion viola una restriccion de la base de datos (por ejemplo, el registro tiene otros registros asociados o un dato unico esta repetido)",
                request, null);
    }

    /** Cualquier otra excepcion no controlada explicitamente: no se filtra el detalle interno al cliente. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex, WebRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado en el servidor", request, null);
    }

    private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String message, WebRequest request, List<String> detalles) {
        ErrorResponseDTO body = new ErrorResponseDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getDescription(false).replace("uri=", ""),
                detalles
        );
        return ResponseEntity.status(status).body(body);
    }
}
