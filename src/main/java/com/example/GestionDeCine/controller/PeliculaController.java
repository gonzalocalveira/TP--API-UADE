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

import com.example.GestionDeCine.model.Pelicula;
import com.example.GestionDeCine.service.interfaces.IPeliculaService;

import jakarta.validation.Valid;

/** CRUD de peliculas del catalogo. */
@RestController
@RequestMapping("/api/peliculas")
public class PeliculaController {

    private final IPeliculaService peliculaService;

    public PeliculaController(IPeliculaService peliculaService) {
        this.peliculaService = peliculaService;
    }

    @GetMapping
    public ResponseEntity<List<Pelicula>> obtenerPeliculas() {
        return ResponseEntity.ok(peliculaService.obtenerPeliculas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pelicula> obtenerPeliculaPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(peliculaService.obtenerPeliculaPorId(id));
    }

    @PostMapping
    public ResponseEntity<Pelicula> crearPelicula(@Valid @RequestBody Pelicula pelicula) {
        return ResponseEntity.status(HttpStatus.CREATED).body(peliculaService.crearPelicula(pelicula));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pelicula> modificarPelicula(@PathVariable Integer id, @Valid @RequestBody Pelicula pelicula) {
        return ResponseEntity.ok(peliculaService.modificarPelicula(id, pelicula));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPelicula(@PathVariable Integer id) {
        peliculaService.eliminarPelicula(id);
        return ResponseEntity.noContent().build();
    }
}
