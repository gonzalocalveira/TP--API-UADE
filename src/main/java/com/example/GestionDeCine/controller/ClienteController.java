package com.example.GestionDeCine.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.GestionDeCine.dto.CompraRequestDTO;
import com.example.GestionDeCine.model.Membresia;
import com.example.GestionDeCine.model.Pelicula;
import com.example.GestionDeCine.model.TicketCompra;
import com.example.GestionDeCine.service.interfaces.IClienteService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final IClienteService clienteService;

    public ClienteController(IClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/cartelera")
    public ResponseEntity<List<Pelicula>> verCartelera() {
        return ResponseEntity.ok(clienteService.verCartelera());
    }

    @PostMapping("/compras")
    public ResponseEntity<TicketCompra> comprarEntrada(@Valid @RequestBody CompraRequestDTO compraRequest) {
        TicketCompra ticket = clienteService.comprarEntrada(
                compraRequest.getUsuarioId(),
                compraRequest.getFuncionId(),
                compraRequest.getAsientoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @GetMapping("/{usuarioId}/historial")
    public ResponseEntity<List<TicketCompra>> verHistorial(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(clienteService.verHistorial(usuarioId));
    }

    @GetMapping("/{usuarioId}/membresia")
    public ResponseEntity<Membresia> verMembresia(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(clienteService.verMembresia(usuarioId));
    }
}
