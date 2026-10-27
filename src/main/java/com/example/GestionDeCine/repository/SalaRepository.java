package com.example.GestionDeCine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.GestionDeCine.model.Sala;

/** DAO de Sala. */
@Repository
public interface SalaRepository extends JpaRepository<Sala, Integer> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Integer id);
}
