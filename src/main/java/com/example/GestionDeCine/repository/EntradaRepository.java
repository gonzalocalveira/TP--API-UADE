package com.example.GestionDeCine.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.GestionDeCine.model.Entrada;
import com.example.GestionDeCine.model.EstadoEntrada;

/** DAO de Entrada. */
@Repository
public interface EntradaRepository extends JpaRepository<Entrada, Integer> {

    /**
     * Ids de las butacas con una entrada en el estado indicado para una funcion.
     * Una sola consulta para toda la sala, en vez de una por butaca (evita N+1).
     */
    @Query("select e.asiento.id from Entrada e where e.ticketCompra.funcion.id = :funcionId and e.estado = :estado")
    List<Integer> findAsientoIdsByFuncionIdAndEstado(Integer funcionId, EstadoEntrada estado);

    boolean existsByAsientoId(Integer asientoId);
}
