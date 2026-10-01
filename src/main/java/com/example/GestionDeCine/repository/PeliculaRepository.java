package com.example.GestionDeCine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.GestionDeCine.model.Pelicula;

/** No existia en el diseño original; hacia falta para poder hacer CRUD de peliculas. */
@Repository
public interface PeliculaRepository extends JpaRepository<Pelicula, Integer> {
}
