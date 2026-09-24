package com.example.GestionDeCine.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
public class Membresia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Integer id;

    @OneToOne //indico que la relacion entre Membresia y Usuario es uno a uno
    @JoinColumn(name="usuario_id") //indico que el id del usuario seria la columna "usuario_id"
    private  Usuario usuario;
    private int puntos;

    @Enumerated(EnumType.STRING)
    private TipoMembresia tipoMembresia;

    public Membresia(Integer id, Usuario usuario, int puntos, TipoMembresia tipoMembresia) {
        this.id = id;
        this.usuario = usuario;
        this.puntos = puntos;
        this.tipoMembresia = tipoMembresia;
    }
}
