package com.example.GestionDeCine.service.interfaces;

import java.time.LocalDate;
import java.util.List;

import com.example.GestionDeCine.dto.AsientoResponseDTO;
import com.example.GestionDeCine.dto.ButacaDTO;
import com.example.GestionDeCine.dto.FuncionRequestDTO;
import com.example.GestionDeCine.dto.FuncionResponseDTO;

/** Contrato de la logica de negocio de funciones. */
public interface IFuncionService {

    /** Lista funciones; peliculaId y fecha son filtros opcionales (null = sin filtro). */
    List<FuncionResponseDTO> obtenerFunciones(Integer peliculaId, LocalDate fecha);

    FuncionResponseDTO obtenerFuncionPorId(Integer id);

    /** Falla con 409 si empieza en el pasado o se superpone con otra funcion de la misma sala. */
    FuncionResponseDTO crearFuncion(FuncionRequestDTO dto);

    /** Ademas de las reglas de crear, falla con 409 si ya tiene compras. */
    FuncionResponseDTO modificarFuncion(Integer id, FuncionRequestDTO dto);

    /** Falla con 409 si ya tiene compras. */
    void eliminarFuncion(Integer id);

    /** Todas las butacas de la sala de la funcion, indicando cuales estan ocupadas. */
    List<ButacaDTO> obtenerButacas(Integer funcionId);

    /** Solo las butacas libres de la funcion. */
    List<AsientoResponseDTO> obtenerAsientosDisponibles(Integer funcionId);
}
