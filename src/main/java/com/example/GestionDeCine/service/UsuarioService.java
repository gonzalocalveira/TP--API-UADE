package com.example.GestionDeCine.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.GestionDeCine.exception.AutenticacionException;
import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.model.Usuario;
import com.example.GestionDeCine.repository.UsuarioRepository;
import com.example.GestionDeCine.service.interfaces.IUsuarioService;

/**
 * Implementacion de la logica de negocio de Usuario.
 *
 * Nota sobre la password: en este TPO se guarda en texto plano para
 * mantener el alcance dentro de lo pedido por la catedra (la materia no
 * pide autenticacion con tokens/hashing). En un sistema real este servicio
 * usaria PasswordEncoder (BCrypt) para hashear antes de guardar y para
 * comparar en el login.
 */
@Service
public class UsuarioService implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Usuario> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario obtenerUsuarioPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + id));
    }

    @Override
    public Usuario iniciarSesion(String mail, String password) {
        Usuario usuario = usuarioRepository.findByMail(mail)
                .orElseThrow(() -> new AutenticacionException("Usuario o contraseña incorrectos"));

        if (!usuario.getPassword().equals(password)) {
            // Mismo mensaje que "mail no encontrado" a proposito: no le damos pistas
            // a quien intenta adivinar credenciales sobre cual de los dos datos fallo.
            throw new AutenticacionException("Usuario o contraseña incorrectos");
        }
        return usuario;
    }

    @Override
    public Usuario crearUsuario(Usuario usuario) {
        if (usuarioRepository.existsByMail(usuario.getMail())) {
            throw new BusinessException("Ya existe un usuario registrado con el mail " + usuario.getMail());
        }
        if (usuarioRepository.existsByDni(usuario.getDni())) {
            throw new BusinessException("Ya existe un usuario registrado con el DNI " + usuario.getDni());
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario modificarUsuario(Integer id, Usuario usuario) {
        Usuario usuarioExistente = obtenerUsuarioPorId(id);

        usuarioExistente.setNombreApellido(usuario.getNombreApellido());
        usuarioExistente.setDni(usuario.getDni());
        usuarioExistente.setFechaNacimiento(usuario.getFechaNacimiento());
        usuarioExistente.setMail(usuario.getMail());
        usuarioExistente.setPassword(usuario.getPassword());
        usuarioExistente.setSocio(usuario.isSocio());
        usuarioExistente.setRol(usuario.getRol());

        return usuarioRepository.save(usuarioExistente);
    }

    @Override
    public void eliminarUsuario(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con id " + id);
        }
        usuarioRepository.deleteById(id);
    }

    @Override
    public boolean tienePermiso(Usuario usuario, String operacion) {
        return switch (usuario.getRol()) {
            case CLIENTE -> operacion.equals("VER_CARTELERA")
                    || operacion.equals("COMPRAR_ENTRADA")
                    || operacion.equals("VER_HISTORIAL")
                    || operacion.equals("VER_MEMBRESIA");

            case EMPLEADO -> operacion.equals("VER_ASIENTOS")
                    || operacion.equals("ASIGNAR_ASIENTO")
                    || operacion.equals("VER_SALAS");

            case ADMINISTRADOR -> true;
        };
    }
}
