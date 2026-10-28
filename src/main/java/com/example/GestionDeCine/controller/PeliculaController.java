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

import com.example.GestionDeCine.dto.PeliculaRequestDTO;
import com.example.GestionDeCine.dto.PeliculaResponseDTO;
import com.example.GestionDeCine.service.interfaces.IPeliculaService;

import jakarta.validation.Valid;

/**
 * Endpoints REST del catalogo de peliculas.
 * Capa de comunicacion: recibe el request, valida el body con @Valid, delega en
 * el service y arma la respuesta HTTP. No contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/peliculas")
public class PeliculaController {

    private final IPeliculaService peliculaService;

    public PeliculaController(IPeliculaService peliculaService) {
        this.peliculaService = peliculaService;
    }

    @GetMapping
    public ResponseEntity<List<PeliculaResponseDTO>> obtenerPeliculas() {
        return ResponseEntity.ok(peliculaService.obtenerPeliculas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PeliculaResponseDTO> obtenerPeliculaPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(peliculaService.obtenerPeliculaPorId(id));
    }

    @PostMapping
    public ResponseEntity<PeliculaResponseDTO> crearPelicula(@Valid @RequestBody PeliculaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(peliculaService.crearPelicula(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PeliculaResponseDTO> modificarPelicula(@PathVariable Integer id, @Valid @RequestBody PeliculaRequestDTO dto) {
        return ResponseEntity.ok(peliculaService.modificarPelicula(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPelicula(@PathVariable Integer id) {
        peliculaService.eliminarPelicula(id);
        return ResponseEntity.noContent().build();
    }
}
