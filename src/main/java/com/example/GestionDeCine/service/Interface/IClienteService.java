package com.example.GestionDeCine.service.Interface;

import com.example.GestionDeCine.model.Entrada;
import com.example.GestionDeCine.model.Membresia;
import com.example.GestionDeCine.model.Pelicula;
import com.example.GestionDeCine.model.TicketCompra;

import java.util.List;

public interface IClienteService {
    List<TicketCompra> verHistorial(Integer usuarioid);
    Membresia verMembresia(Integer usuarioId);
    TicketCompra comprarEntrada( Integer usuarioId, Integer funcionId, Integer asientoId);
    List<Pelicula> verCartelera();
    List<Entrada> verEntradas();
}
