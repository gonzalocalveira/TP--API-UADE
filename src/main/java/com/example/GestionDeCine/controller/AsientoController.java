package com.example.GestionDeCine.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.GestionDeCine.dto.AsientoRequestDTO;
import com.example.GestionDeCine.dto.AsientoResponseDTO;
import com.example.GestionDeCine.service.interfaces.IAsientoService;

import jakarta.validation.Valid;

/**
 * Endpoints REST de butacas.
 * Capa de comunicacion: recibe el request, valida el body con @Valid, delega en
 * el service y arma la respuesta HTTP. No contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/asientos")
public class AsientoController {

    private final IAsientoService asientoService;

    public AsientoController(IAsientoService asientoService) {
        this.asientoService = asientoService;
    }

    @GetMapping
    public ResponseEntity<List<AsientoResponseDTO>> obtenerAsientos() {
        return ResponseEntity.ok(asientoService.obtenerAsientos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AsientoResponseDTO> obtenerAsientoPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(asientoService.obtenerAsientoPorId(id));
    }

    @PostMapping
    public ResponseEntity<AsientoResponseDTO> crearAsiento(@Valid @RequestBody AsientoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asientoService.crearAsiento(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AsientoResponseDTO> modificarAsiento(@PathVariable Integer id, @Valid @RequestBody AsientoRequestDTO dto) {
        return ResponseEntity.ok(asientoService.modificarAsiento(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAsiento(@PathVariable Integer id) {
        asientoService.eliminarAsiento(id);
        return ResponseEntity.noContent().build();
    }
}
