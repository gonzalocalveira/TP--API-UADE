package com.example.GestionDeCine.service.interfaces;

import java.util.List;

import com.example.GestionDeCine.model.Asiento;
import com.example.GestionDeCine.model.Funcion;

public interface IFuncionService {
    List<Funcion> obtenerFunciones();

    Funcion obtenerFuncionPorId(Integer id);

    Funcion crearFuncion(Funcion funcion);

    Funcion modificarFuncion(Integer id, Funcion funcion);

    void eliminarFuncion(Integer id);

    /** Asientos de la sala de la funcion que todavia NO fueron comprados para esa funcion puntual. */
    List<Asiento> obtenerAsientosDisponibles(Integer funcionId);
}
