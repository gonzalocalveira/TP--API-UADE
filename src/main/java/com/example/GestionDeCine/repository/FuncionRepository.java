package com.example.GestionDeCine.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.GestionDeCine.model.Funcion;

import jakarta.persistence.LockModeType;

/** DAO de Funcion. */
@Repository
public interface FuncionRepository extends JpaRepository<Funcion, Integer> {

    /**
     * Lista funciones con filtros opcionales (si un parametro es null, no filtra).
     * "join fetch" trae pelicula y sala en la misma consulta (evita N+1 al armar los DTOs).
     */
    @Query("""
            select f from Funcion f join fetch f.pelicula join fetch f.sala
            where (:peliculaId is null or f.pelicula.id = :peliculaId)
              and (:fecha is null or f.fechaFuncion = :fecha)
            order by f.fechaFuncion, f.horaFuncion
            """)
    List<Funcion> buscar(Integer peliculaId, LocalDate fecha);

    /** Funciones de una sala en un rango de fechas, con su pelicula (para calcular superposiciones). */
    @Query("""
            select f from Funcion f join fetch f.pelicula
            where f.sala.id = :salaId and f.fechaFuncion between :desde and :hasta
            """)
    List<Funcion> findBySalaEntreFechas(Integer salaId, LocalDate desde, LocalDate hasta);

    /**
     * Busca la funcion y bloquea su fila (SELECT ... FOR UPDATE) hasta que termine la transaccion.
     * Si dos personas compran para la misma funcion a la vez, la segunda espera a que la primera
     * termine y recien ahi ve las butacas ya ocupadas: asi nunca se vende dos veces la misma butaca.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Funcion f where f.id = :id")
    Optional<Funcion> findByIdParaCompra(Integer id);

    boolean existsByPeliculaId(Integer peliculaId);

    boolean existsBySalaId(Integer salaId);
}
