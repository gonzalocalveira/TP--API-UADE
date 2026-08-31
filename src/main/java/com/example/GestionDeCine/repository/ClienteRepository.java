package com.example.GestionDeCine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ClienteRepository<T> extends JpaRepository<T,Integer> {

}
