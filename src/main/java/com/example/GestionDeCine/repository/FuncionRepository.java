package com.example.GestionDeCine.repository;

import com.example.GestionDeCine.model.Funcion;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FuncionRepository extends JpaRepository<Funcion, Integer> {
    Optional <Funcion> findById(Integer funcionId);
}
