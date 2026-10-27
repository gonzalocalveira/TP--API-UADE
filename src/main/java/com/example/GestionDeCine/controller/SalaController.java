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

import com.example.GestionDeCine.dto.SalaRequestDTO;
import com.example.GestionDeCine.dto.SalaResponseDTO;
import com.example.GestionDeCine.dto.AsientoResponseDTO;
import com.example.GestionDeCine.service.interfaces.IAsientoService;
import com.example.GestionDeCine.service.interfaces.ISalaService;

import jakarta.validation.Valid;

/**
 * Endpoints REST de salas.
 * Capa de comunicacion: recibe el request, valida el body con @Valid, delega en
 * el service y arma la respuesta HTTP. No contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/salas")
public class SalaController {

    private final ISalaService salaService;
    private final IAsientoService asientoService;

    public SalaController(ISalaService salaService, IAsientoService asientoService) {
        this.salaService = salaService;
        this.asientoService = asientoService;
    }

    @GetMapping
    public ResponseEntity<List<SalaResponseDTO>> obtenerSalas() {
        return ResponseEntity.ok(salaService.obtenerSalas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalaResponseDTO> obtenerSalaPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(salaService.obtenerSalaPorId(id));
    }

    @PostMapping
    public ResponseEntity<SalaResponseDTO> crearSala(@Valid @RequestBody SalaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salaService.crearSala(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalaResponseDTO> modificarSala(@PathVariable Integer id, @Valid @RequestBody SalaRequestDTO dto) {
        return ResponseEntity.ok(salaService.modificarSala(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarSala(@PathVariable Integer id) {
        salaService.eliminarSala(id);
        return ResponseEntity.noContent().build();
    }

    /** Butacas de una sala. */
    @GetMapping("/{id}/asientos")
    public ResponseEntity<List<AsientoResponseDTO>> obtenerAsientosDeLaSala(@PathVariable Integer id) {
        return ResponseEntity.ok(asientoService.obtenerAsientosPorSala(id));
    }
}
