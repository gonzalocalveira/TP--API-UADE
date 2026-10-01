package com.example.GestionDeCine.service.interfaces;

import java.util.List;

import com.example.GestionDeCine.model.Asiento;

public interface IAsientoService {
    List<Asiento> obtenerAsientos();

    List<Asiento> obtenerAsientosPorSala(Integer salaId);

    Asiento obtenerAsientoPorId(Integer id);

    Asiento crearAsiento(Asiento asiento);

    Asiento modificarAsiento(Integer id, Asiento asiento);

    void eliminarAsiento(Integer id);
}
