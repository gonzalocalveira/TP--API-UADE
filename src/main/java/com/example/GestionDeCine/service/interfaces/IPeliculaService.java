package com.example.GestionDeCine.service.interfaces;

import java.util.List;

import com.example.GestionDeCine.model.Pelicula;

public interface IPeliculaService {
    List<Pelicula> obtenerPeliculas();

    Pelicula obtenerPeliculaPorId(Integer id);

    Pelicula crearPelicula(Pelicula pelicula);

    Pelicula modificarPelicula(Integer id, Pelicula pelicula);

    void eliminarPelicula(Integer id);
}
