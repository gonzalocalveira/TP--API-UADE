package com.example.GestionDeCine.repository;

import com.example.GestionDeCine.model.Entrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.yaml.snakeyaml.events.Event;

public interface EntradaRepository extends JpaRepository<Entrada, Integer> {
    boolean isOcupado(Integer funcionId, Integer asientoId);
}
