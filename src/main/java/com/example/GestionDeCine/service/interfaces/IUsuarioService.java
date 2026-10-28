package com.example.GestionDeCine.service.interfaces;

import java.util.List;

import com.example.GestionDeCine.dto.UsuarioRequestDTO;
import com.example.GestionDeCine.dto.UsuarioResponseDTO;

/** Contrato de la logica de negocio de usuarios. */
public interface IUsuarioService {

    List<UsuarioResponseDTO> obtenerUsuarios();

    UsuarioResponseDTO obtenerUsuarioPorId(Integer id);

    /** Devuelve el usuario si mail y password coinciden; si no, 401. */
    UsuarioResponseDTO iniciarSesion(String mail, String password);

    /** Registra siempre un CLIENTE no socio. Falla con 409 si el mail o el DNI ya existen. */
    UsuarioResponseDTO crearUsuario(UsuarioRequestDTO dto);

    UsuarioResponseDTO modificarUsuario(Integer id, UsuarioRequestDTO dto);

    /** Falla con 409 si el usuario tiene compras. */
    void eliminarUsuario(Integer id);
}
