package com.example.GestionDeCine.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.GestionDeCine.model.TicketCompra;

/**
 * El diseño original declaraba
 * "JpaRepository<TicketCompraRepository, Integer>", parametrizando el
 * repositorio con si mismo en vez de con la entidad TicketCompra: no
 * compilaba. Se corrige a JpaRepository<TicketCompra, Integer>.
 */
@Repository
public interface TicketCompraRepository extends JpaRepository<TicketCompra, Integer> {
    List<TicketCompra> findByUsuarioId(Integer usuarioId);
}
