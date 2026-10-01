package com.example.GestionDeCine.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.GestionDeCine.model.Asiento;

@Repository
public interface AsientoRepository extends JpaRepository<Asiento, Integer> {
    // findById ya lo provee JpaRepository; el original tenia un typo
    // ("finddById") que impedia siquiera compilar.
    List<Asiento> findBySalaId(Integer salaId);
}
