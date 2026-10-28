package com.example.GestionDeCine.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.GestionDeCine.dto.CompraRequestDTO;
import com.example.GestionDeCine.dto.MembresiaResponseDTO;
import com.example.GestionDeCine.dto.PeliculaResponseDTO;
import com.example.GestionDeCine.dto.TicketCompraResponseDTO;
import com.example.GestionDeCine.service.interfaces.IClienteService;

import jakarta.validation.Valid;

/** Endpoints del "e-commerce": cartelera, compra y cancelacion de entradas, historial y membresia. */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final IClienteService clienteService;

    public ClienteController(IClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/cartelera")
    public ResponseEntity<List<PeliculaResponseDTO>> verCartelera() {
        return ResponseEntity.ok(clienteService.verCartelera());
    }

    @PostMapping("/compras")
    public ResponseEntity<TicketCompraResponseDTO> comprarEntradas(@Valid @RequestBody CompraRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.comprarEntradas(dto));
    }

    /** PATCH porque modifica solo una parte del recurso (su estado), no lo reemplaza entero. */
    @PatchMapping("/compras/{id}/cancelar")
    public ResponseEntity<TicketCompraResponseDTO> cancelarCompra(@PathVariable Integer id) {
        return ResponseEntity.ok(clienteService.cancelarCompra(id));
    }

    @GetMapping("/{usuarioId}/historial")
    public ResponseEntity<List<TicketCompraResponseDTO>> verHistorial(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(clienteService.verHistorial(usuarioId));
    }

    @GetMapping("/{usuarioId}/membresia")
    public ResponseEntity<MembresiaResponseDTO> verMembresia(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(clienteService.verMembresia(usuarioId));
    }
}
