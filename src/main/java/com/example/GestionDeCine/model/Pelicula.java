package com.example.GestionDeCine.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pelicula del catalogo. Mapea a la tabla "pelicula".
 * Las validaciones de formato estan en PeliculaRequestDTO; la entidad solo
 * describe como se guardan los datos.
 */
@Entity
@Table(name = "pelicula")
@Getter
@Setter
@NoArgsConstructor
public class Pelicula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "director", nullable = false, length = 100)
    private String director;

    /** Duracion en minutos; se usa para calcular la superposicion de funciones. */
    @Column(name = "duracion", nullable = false)
    private Integer duracion;

    @Column(name = "fecha_estreno", nullable = false)
    private LocalDate fechaEstreno;

    @Column(name = "idioma", nullable = false, length = 50)
    private String idioma;

    @Column(name = "genero", nullable = false, length = 50)
    private String genero;

    @Column(name = "clasificacion_edad", nullable = false, length = 20)
    private String clasificacionEdad;

    @Column(name = "sinopsis", nullable = false, length = 1000)
    private String sinopsis;
}
