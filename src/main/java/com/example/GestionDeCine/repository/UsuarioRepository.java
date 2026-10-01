package com.example.GestionDeCine.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.GestionDeCine.model.Usuario;

/**
 * Capa DAO para Usuario. Extender JpaRepository<Usuario, Integer> le da a
 * esta interfaz, sin escribir una sola linea de SQL, los metodos
 * save/findById/findAll/deleteById/etc. Spring Data JPA genera la
 * implementacion en tiempo de ejecucion (proxy dinamico).
 *
 * Los metodos "findByMail" / "existsByMail" son "query methods": Spring
 * Data interpreta el nombre del metodo y arma la consulta SQL
 * automaticamente (SELECT * FROM usuarios WHERE mail = ?).
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByMail(String mail);

    boolean existsByMail(String mail);

    boolean existsByDni(Integer dni);
}
