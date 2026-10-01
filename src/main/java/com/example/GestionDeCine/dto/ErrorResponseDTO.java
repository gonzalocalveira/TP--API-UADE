package com.example.GestionDeCine.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Formato unico de respuesta de error para toda la API (ver
 * exception.GlobalExceptionHandler). Que todos los errores tengan la misma
 * forma facilita mucho el trabajo del front-end en la Etapa 2.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> detalles;
}
