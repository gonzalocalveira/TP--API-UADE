package com.example.GestionDeCine.mapper;

import com.example.GestionDeCine.dto.UsuarioRequestDTO;
import com.example.GestionDeCine.dto.UsuarioResponseDTO;
import com.example.GestionDeCine.model.Usuario;

/** Convierte Usuario (entidad) a y desde sus DTOs. La password nunca pasa al DTO de respuesta. */
public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombreApellido(),
                usuario.getDni(),
                usuario.getFechaNacimiento(),
                usuario.getMail(),
                usuario.isSocio(),
                usuario.getRol());
    }

    public static void copiarDatos(UsuarioRequestDTO dto, Usuario usuario) {
        usuario.setNombreApellido(dto.getNombreApellido().trim());
        usuario.setDni(dto.getDni());
        usuario.setFechaNacimiento(dto.getFechaNacimiento());
        usuario.setMail(dto.getMail().trim().toLowerCase());
        usuario.setPassword(dto.getPassword());
    }
}
