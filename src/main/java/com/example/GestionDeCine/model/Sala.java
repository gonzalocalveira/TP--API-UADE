package com.example.GestionDeCine.model;


import jakarta.persistence.Entity;
import lombok.*;

import java.util.List;

@Entity
@Data
public class Sala {
    private List<Pelicula> peliculasList;

}
