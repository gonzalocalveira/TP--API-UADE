package com.example.GestionDeCine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.GestionDeCine.model.Sala;

/** No existia en el diseño original; hacia falta para poder hacer CRUD de salas. */
@Repository
public interface SalaRepository extends JpaRepository<Sala, Integer> {
}
