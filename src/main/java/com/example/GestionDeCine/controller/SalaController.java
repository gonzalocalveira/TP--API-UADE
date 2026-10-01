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

import com.example.GestionDeCine.model.Sala;
import com.example.GestionDeCine.service.interfaces.IAsientoService;
import com.example.GestionDeCine.service.interfaces.ISalaService;
import com.example.GestionDeCine.model.Asiento;

import jakarta.validation.Valid;

/** CRUD de salas, mas la consulta de los asientos de una sala. */
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
    public ResponseEntity<List<Sala>> obtenerSalas() {
        return ResponseEntity.ok(salaService.obtenerSalas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sala> obtenerSalaPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(salaService.obtenerSalaPorId(id));
    }

    @PostMapping
    public ResponseEntity<Sala> crearSala(@Valid @RequestBody Sala sala) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salaService.crearSala(sala));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sala> modificarSala(@PathVariable Integer id, @Valid @RequestBody Sala sala) {
        return ResponseEntity.ok(salaService.modificarSala(id, sala));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarSala(@PathVariable Integer id) {
        salaService.eliminarSala(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/asientos")
    public ResponseEntity<List<Asiento>> obtenerAsientosDeLaSala(@PathVariable Integer id) {
        return ResponseEntity.ok(asientoService.obtenerAsientosPorSala(id));
    }
}
