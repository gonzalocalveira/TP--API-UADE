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

import com.example.GestionDeCine.dto.LoginRequestDTO;
import com.example.GestionDeCine.dto.UsuarioResponseDTO;
import com.example.GestionDeCine.model.Usuario;
import com.example.GestionDeCine.service.interfaces.IUsuarioService;

import jakarta.validation.Valid;

/**
 * Expone las operaciones CRUD de Usuario y el login.
 *
 * Capa Controller: SOLO se encarga de traducir HTTP <-> objetos Java
 * (deserializar el body, delegar en el Service, elegir el status code de
 * la respuesta). No contiene reglas de negocio: esas viven en
 * IUsuarioService/UsuarioService. Esto responde directamente a la pregunta
 * guia "que ocurriria si colocaramos toda la logica en el Controller?":
 * el controller quedaria acoplado a HTTP y no se podria reutilizar la
 * logica desde otro punto de entrada (por ejemplo, un job interno).
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final IUsuarioService usuarioService;

    public UsuarioController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerUsuarios() {
        List<UsuarioResponseDTO> usuarios = usuarioService.obtenerUsuarios().stream()
                .map(this::toResponseDTO)
                .toList();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuarioPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(toResponseDTO(usuarioService.obtenerUsuarioPorId(id)));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crearUsuario(@Valid @RequestBody Usuario usuario) {
        Usuario creado = usuarioService.crearUsuario(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> modificarUsuario(@PathVariable Integer id,
                                                                 @Valid @RequestBody Usuario usuario) {
        return ResponseEntity.ok(toResponseDTO(usuarioService.modificarUsuario(id, usuario)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Integer id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        Usuario usuario = usuarioService.iniciarSesion(loginRequest.getMail(), loginRequest.getPassword());
        return ResponseEntity.ok(toResponseDTO(usuario));
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombreApellido(),
                usuario.getDni(),
                usuario.getFechaNacimiento(),
                usuario.getMail(),
                usuario.isSocio(),
                usuario.getRol()
        );
    }
}
