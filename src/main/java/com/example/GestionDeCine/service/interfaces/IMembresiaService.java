package com.example.GestionDeCine.service.interfaces;

import com.example.GestionDeCine.dto.MembresiaRequestDTO;
import com.example.GestionDeCine.dto.MembresiaResponseDTO;

/** Contrato de la logica de negocio de membresias (programa de socios). */
public interface IMembresiaService {

    /** Da de alta la membresia y marca al usuario como socio. Falla con 409 si ya tiene una. */
    MembresiaResponseDTO crearMembresia(MembresiaRequestDTO dto);

    /** Elimina la membresia y el usuario deja de ser socio. */
    void eliminarMembresia(Integer id);
}
