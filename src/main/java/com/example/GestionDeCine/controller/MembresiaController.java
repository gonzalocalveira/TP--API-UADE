package com.example.GestionDeCine.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.GestionDeCine.dto.MembresiaRequestDTO;
import com.example.GestionDeCine.dto.MembresiaResponseDTO;
import com.example.GestionDeCine.service.interfaces.IMembresiaService;

import jakarta.validation.Valid;

/** Endpoints REST de membresias. Delega toda la logica en IMembresiaService. */
@RestController
@RequestMapping("/api/membresias")
public class MembresiaController {

    private final IMembresiaService membresiaService;

    public MembresiaController(IMembresiaService membresiaService) {
        this.membresiaService = membresiaService;
    }

    @PostMapping
    public ResponseEntity<MembresiaResponseDTO> crearMembresia(@Valid @RequestBody MembresiaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membresiaService.crearMembresia(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMembresia(@PathVariable Integer id) {
        membresiaService.eliminarMembresia(id);
        return ResponseEntity.noContent().build();
    }
}
