package com.example.GestionDeCine.service.interfaces;

import java.util.List;

import com.example.GestionDeCine.model.Usuario;

/**
 * Contrato de la logica de negocio relacionada a Usuario. Separar la
 * interfaz de la implementacion (UsuarioService) permite, por ejemplo,
 * reemplazar la implementacion en tests (mocks) sin tocar los Controllers,
 * que solo dependen de esta interfaz.
 */
public interface IUsuarioService {

    List<Usuario> obtenerUsuarios();

    Usuario obtenerUsuarioPorId(Integer id);

    Usuario iniciarSesion(String mail, String password);

    boolean tienePermiso(Usuario usuario, String operacion);

    Usuario crearUsuario(Usuario usuario);

    Usuario modificarUsuario(Integer id, Usuario usuario);

    void eliminarUsuario(Integer id);
}
