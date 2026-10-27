package com.example.GestionDeCine.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.GestionDeCine.dto.AsientoResponseDTO;
import com.example.GestionDeCine.dto.ButacaDTO;
import com.example.GestionDeCine.dto.FuncionRequestDTO;
import com.example.GestionDeCine.dto.FuncionResponseDTO;
import com.example.GestionDeCine.service.interfaces.IFuncionService;

import jakarta.validation.Valid;

/**
 * Endpoints REST de funciones y de disponibilidad de butacas.
 * Capa de comunicacion: no contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/funciones")
public class FuncionController {

    private final IFuncionService funcionService;

    public FuncionController(IFuncionService funcionService) {
        this.funcionService = funcionService;
    }

    /** Lista funciones. Filtros opcionales: ?peliculaId=1 y/o ?fecha=2026-10-28 */
    @GetMapping
    public ResponseEntity<List<FuncionResponseDTO>> obtenerFunciones(
            @RequestParam(required = false) Integer peliculaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(funcionService.obtenerFunciones(peliculaId, fecha));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionResponseDTO> obtenerFuncionPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(funcionService.obtenerFuncionPorId(id));
    }

    @PostMapping
    public ResponseEntity<FuncionResponseDTO> crearFuncion(@Valid @RequestBody FuncionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(funcionService.crearFuncion(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FuncionResponseDTO> modificarFuncion(@PathVariable Integer id, @Valid @RequestBody FuncionRequestDTO dto) {
        return ResponseEntity.ok(funcionService.modificarFuncion(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFuncion(@PathVariable Integer id) {
        funcionService.eliminarFuncion(id);
        return ResponseEntity.noContent().build();
    }

    /** Mapa de la sala para esta funcion: todas las butacas con su estado (ocupada o libre). */
    @GetMapping("/{id}/butacas")
    public ResponseEntity<List<ButacaDTO>> obtenerButacas(@PathVariable Integer id) {
        return ResponseEntity.ok(funcionService.obtenerButacas(id));
    }

    /** Solo las butacas que todavia se pueden comprar para esta funcion. */
    @GetMapping("/{id}/asientos-disponibles")
    public ResponseEntity<List<AsientoResponseDTO>> obtenerAsientosDisponibles(@PathVariable Integer id) {
        return ResponseEntity.ok(funcionService.obtenerAsientosDisponibles(id));
    }
}
