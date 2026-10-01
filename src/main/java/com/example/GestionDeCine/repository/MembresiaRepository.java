package com.example.GestionDeCine.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.GestionDeCine.model.Membresia;

@Repository
public interface MembresiaRepository extends JpaRepository<Membresia, Integer> {

    Optional<Membresia> findByUsuarioId(Integer usuarioId);

    boolean existsByUsuarioId(Integer usuarioId);
}
