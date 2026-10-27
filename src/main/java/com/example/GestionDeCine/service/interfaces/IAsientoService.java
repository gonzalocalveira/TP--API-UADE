package com.example.GestionDeCine.service.interfaces;

import java.util.List;

import com.example.GestionDeCine.dto.AsientoRequestDTO;
import com.example.GestionDeCine.dto.AsientoResponseDTO;

/** Contrato de la logica de negocio de butacas. */
public interface IAsientoService {

    List<AsientoResponseDTO> obtenerAsientos();

    List<AsientoResponseDTO> obtenerAsientosPorSala(Integer salaId);

    AsientoResponseDTO obtenerAsientoPorId(Integer id);

    AsientoResponseDTO crearAsiento(AsientoRequestDTO dto);

    AsientoResponseDTO modificarAsiento(Integer id, AsientoRequestDTO dto);

    /** Falla con 409 si la butaca ya fue vendida alguna vez. */
    void eliminarAsiento(Integer id);
}
