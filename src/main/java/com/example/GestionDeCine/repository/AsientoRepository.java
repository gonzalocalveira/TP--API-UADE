package com.example.GestionDeCine.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.GestionDeCine.model.Asiento;

/** DAO de Asiento. Los metodos se implementan solos a partir de su nombre (query methods). */
@Repository
public interface AsientoRepository extends JpaRepository<Asiento, Integer> {

    List<Asiento> findBySalaIdOrderByFilaAsientoAscNumeroAsientoAsc(Integer salaId);

    long countBySalaId(Integer salaId);

    boolean existsBySalaId(Integer salaId);

    boolean existsBySalaIdAndFilaAsientoIgnoreCaseAndNumeroAsiento(Integer salaId, String filaAsiento, Integer numeroAsiento);
}
