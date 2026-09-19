package com.example.GestionDeCine.repository;

import com.example.GestionDeCine.model.Asiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AsientoRepository extends JpaRepository<Asiento, Integer> {
    Optional<Asiento> finddById(Integer asientoId);
}
