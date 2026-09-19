package com.example.GestionDeCine.service;

import com.example.GestionDeCine.model.Usuario;
import com.example.GestionDeCine.service.Interface.IUsuarioService;
import com.example.GestionDeCine.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
    public class UsuarioService implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository clienteRepository) {
        this.usuarioRepository = clienteRepository;
    }

    @Override
    public Usuario iniciarSesion(String mail, String password) {

        Usuario usuario = usuarioRepository.findByMail(mail)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        if (!usuario.getPassword().equals(password)) {
            throw new RuntimeException("Contraseña incorrecta");

        }

        return usuario;
    }

    @Override
    public boolean tienePermiso(Usuario usuario, String operacion) {

        switch (usuario.getRol()){
            case CLIENTE:
                return operacion.equals("VER_CARTERALERA") ||
                        operacion.equals("COMPRAR_ENTRADA")||
                        operacion.equals("VER_HISTORIAL")||
                        operacion.equals("VER_MEMBRESIA");

            case EMPLEADO:
                return operacion.equals("VER_ASIENTOS")
                        || operacion.equals("ASIGNAR_ASIENTO")
                        || operacion.equals("VER_SALAS");

            case ADMINISTRADOR:
                return true;

            default:
                return false;
        }

    }


}
