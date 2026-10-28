package com.example.GestionDeCine.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.GestionDeCine.model.TicketCompra;

/** DAO de TicketCompra. */
@Repository
public interface TicketCompraRepository extends JpaRepository<TicketCompra, Integer> {

    /** Historial de un usuario. @EntityGraph trae en la misma consulta lo que necesita el DTO (evita N+1). */
    @EntityGraph(attributePaths = { "funcion.pelicula", "funcion.sala", "entradas.asiento" })
    List<TicketCompra> findByUsuarioIdOrderByFechaCompraDesc(Integer usuarioId);

    boolean existsByUsuarioId(Integer usuarioId);

    boolean existsByFuncionId(Integer funcionId);
}
