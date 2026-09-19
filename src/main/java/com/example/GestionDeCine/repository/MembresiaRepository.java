package com.example.GestionDeCine.repository;

import com.example.GestionDeCine.model.Membresia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembresiaRepository extends JpaRepository<Membresia, Integer> {

    Membresia findByUsuarioId( Integer usuarioId);


}
