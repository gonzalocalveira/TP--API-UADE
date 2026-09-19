package com.example.GestionDeCine.repository;

import com.example.GestionDeCine.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Integer> {
    Optional<Usuario> findByMail(String mail);

}
