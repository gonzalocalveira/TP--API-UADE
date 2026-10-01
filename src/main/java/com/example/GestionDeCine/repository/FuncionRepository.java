package com.example.GestionDeCine.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.GestionDeCine.model.Funcion;

@Repository
public interface FuncionRepository extends JpaRepository<Funcion, Integer> {
    List<Funcion> findByPeliculaId(Integer peliculaId);

    List<Funcion> findByFechaFuncionGreaterThanEqual(LocalDate fecha);
}
