package com.example.GestionDeCine.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Programa de socios: un usuario tiene como maximo una membresia (1 a 1). */
@Entity
@Table(name = "membresia")
@Getter
@Setter
@NoArgsConstructor
public class Membresia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // unique = true en la FK es lo que hace que la relacion sea 1 a 1 en la base.
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "puntos", nullable = false)
    private int puntos;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_membresia", nullable = false)
    private TipoMembresia tipoMembresia;
}
