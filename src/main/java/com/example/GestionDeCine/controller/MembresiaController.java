package com.example.GestionDeCine.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.model.Membresia;
import com.example.GestionDeCine.repository.MembresiaRepository;
import com.example.GestionDeCine.repository.UsuarioRepository;

import jakarta.validation.Valid;

/**
 * Alta y baja de membresias. Se mantiene aparte de ClienteController (que
 * solo consulta la membresia del propio cliente) porque dar de alta una
 * membresia es tipicamente una operacion administrativa/de empleado.
 * Al ser una entidad simple, no se creo una capa de Service dedicada: la
 * unica regla de negocio (que el usuario no tenga ya una membresia) se
 * valida aqui mismo contra el repository; si el TPO creciera, esta logica
 * se extraeria a un MembresiaService.
 */
@RestController
@RequestMapping("/api/membresias")
public class MembresiaController {

    private final MembresiaRepository membresiaRepository;
    private final UsuarioRepository usuarioRepository;

    public MembresiaController(MembresiaRepository membresiaRepository, UsuarioRepository usuarioRepository) {
        this.membresiaRepository = membresiaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    public ResponseEntity<Membresia> crearMembresia(@Valid @RequestBody Membresia membresia) {
        Integer usuarioId = membresia.getUsuario().getId();

        var usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + usuarioId));

        if (membresiaRepository.existsByUsuarioId(usuarioId)) {
            throw new BusinessException("El usuario " + usuarioId + " ya tiene una membresia");
        }

        membresia.setUsuario(usuario);
        Membresia creada = membresiaRepository.save(membresia);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMembresia(@PathVariable Integer id) {
        if (!membresiaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Membresia no encontrada con id " + id);
        }
        membresiaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
