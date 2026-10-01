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
import com.example.GestionDeCine.model.Funcion;
import com.example.GestionDeCine.service.interfaces.IFuncionService;

import jakarta.validation.Valid;

/** CRUD de funciones (pelicula + sala + horario) y disponibilidad de asientos. */
@RestController
@RequestMapping("/api/funciones")
public class FuncionController {

    private final IFuncionService funcionService;

    public FuncionController(IFuncionService funcionService) {
        this.funcionService = funcionService;
    }

    @GetMapping
    public ResponseEntity<List<Funcion>> obtenerFunciones() {
        return ResponseEntity.ok(funcionService.obtenerFunciones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Funcion> obtenerFuncionPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(funcionService.obtenerFuncionPorId(id));
    }

    @PostMapping
    public ResponseEntity<Funcion> crearFuncion(@Valid @RequestBody Funcion funcion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(funcionService.crearFuncion(funcion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Funcion> modificarFuncion(@PathVariable Integer id, @Valid @RequestBody Funcion funcion) {
        return ResponseEntity.ok(funcionService.modificarFuncion(id, funcion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFuncion(@PathVariable Integer id) {
        funcionService.eliminarFuncion(id);
        return ResponseEntity.noContent().build();
    }

    /** Asientos de la sala de la funcion que todavia se pueden comprar. */
    @GetMapping("/{id}/asientos-disponibles")
    public ResponseEntity<List<Asiento>> obtenerAsientosDisponibles(@PathVariable Integer id) {
        return ResponseEntity.ok(funcionService.obtenerAsientosDisponibles(id));
    }
}
