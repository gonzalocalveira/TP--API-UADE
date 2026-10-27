package com.example.GestionDeCine.service.interfaces;

import java.util.List;

import com.example.GestionDeCine.dto.SalaRequestDTO;
import com.example.GestionDeCine.dto.SalaResponseDTO;

/** Contrato de la logica de negocio de salas. */
public interface ISalaService {

    List<SalaResponseDTO> obtenerSalas();

    SalaResponseDTO obtenerSalaPorId(Integer id);

    SalaResponseDTO crearSala(SalaRequestDTO dto);

    SalaResponseDTO modificarSala(Integer id, SalaRequestDTO dto);

    /** Falla con 409 si la sala tiene butacas o funciones. */
    void eliminarSala(Integer id);
}
