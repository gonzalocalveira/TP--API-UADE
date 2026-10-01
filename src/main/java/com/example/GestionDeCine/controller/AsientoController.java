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

import com.example.GestionDeCine.model.Asiento;
import com.example.GestionDeCine.service.interfaces.IAsientoService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/asientos")
public class AsientoController {

    private final IAsientoService asientoService;

    public AsientoController(IAsientoService asientoService) {
        this.asientoService = asientoService;
    }

    @GetMapping
    public ResponseEntity<List<Asiento>> obtenerAsientos() {
        return ResponseEntity.ok(asientoService.obtenerAsientos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Asiento> obtenerAsientoPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(asientoService.obtenerAsientoPorId(id));
    }

    @PostMapping
    public ResponseEntity<Asiento> crearAsiento(@Valid @RequestBody Asiento asiento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asientoService.crearAsiento(asiento));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Asiento> modificarAsiento(@PathVariable Integer id, @Valid @RequestBody Asiento asiento) {
        return ResponseEntity.ok(asientoService.modificarAsiento(id, asiento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAsiento(@PathVariable Integer id) {
        asientoService.eliminarAsiento(id);
        return ResponseEntity.noContent().build();
    }
}
