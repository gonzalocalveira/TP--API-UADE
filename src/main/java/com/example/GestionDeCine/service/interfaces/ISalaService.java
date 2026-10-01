package com.example.GestionDeCine.service.interfaces;

import java.util.List;

import com.example.GestionDeCine.model.Sala;

public interface ISalaService {
    List<Sala> obtenerSalas();

    Sala obtenerSalaPorId(Integer id);

    Sala crearSala(Sala sala);

    Sala modificarSala(Integer id, Sala sala);

    void eliminarSala(Integer id);
}
