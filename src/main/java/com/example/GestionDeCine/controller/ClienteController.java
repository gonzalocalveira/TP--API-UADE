package com.example.GestionDeCine.controller;

import com.example.GestionDeCine.model.Usuario;
import com.example.GestionDeCine.service.UsuarioService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;
@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final UsuarioService clienteService;

    public ClienteController(UsuarioService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<Usuario> obtenerClientes() {
        return clienteService.obtenerClientes();
    }

    @GetMapping("/{id}")
    public Usuario obtenerCliente(@PathVariable Integer id) {
        return clienteService.obtenerCliente(id);
    }

    @PostMapping
    public Usuario crearCliente(@RequestBody Usuario usuario) {
        return clienteService.crearCliente(usuario);
    }

    @DeleteMapping("/{id}")
    public void eliminarCliente(@PathVariable Integer id) {
        clienteService.eliminarCliente(id);
    }
}
