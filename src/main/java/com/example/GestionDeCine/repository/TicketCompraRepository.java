package com.example.GestionDeCine.repository;

import com.example.GestionDeCine.model.TicketCompra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketCompraRepository extends JpaRepository<TicketCompraRepository, Integer> {
    List<TicketCompra> findByUsuarioId(Integer usuarioId);
}

