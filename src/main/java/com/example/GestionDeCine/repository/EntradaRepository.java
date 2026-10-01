package com.example.GestionDeCine.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.GestionDeCine.model.EstadoEntrada;
import com.example.GestionDeCine.model.Entrada;

/**
 * El diseño original declaraba "boolean isOcupado(...)" sin @Query ni una
 * convencion de nombre que Spring Data pudiera interpretar (no compilaba).
 * Se reemplaza por "existsByFuncionIdAndAsientoIdAndEstado", que Spring
 * Data traduce automaticamente a:
 *   SELECT COUNT(*) > 0 FROM entrada
 *   WHERE funcion_id = ? AND asiento_id = ? AND estado = ?
 */
@Repository
public interface EntradaRepository extends JpaRepository<Entrada, Integer> {

    boolean existsByFuncionIdAndAsientoIdAndEstado(Integer funcionId, Integer asientoId, EstadoEntrada estado);

    List<Entrada> findByFuncionIdAndEstado(Integer funcionId, EstadoEntrada estado);
}
