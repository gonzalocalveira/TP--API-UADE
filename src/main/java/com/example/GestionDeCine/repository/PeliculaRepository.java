package com.example.GestionDeCine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.GestionDeCine.model.Pelicula;

/** DAO de Pelicula: el CRUD basico (save, findById, findAll, delete...) lo hereda de JpaRepository. */
@Repository
public interface PeliculaRepository extends JpaRepository<Pelicula, Integer> {
}
