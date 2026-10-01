package com.example.GestionDeCine.service.interfaces;

import java.util.List;

import com.example.GestionDeCine.model.Membresia;
import com.example.GestionDeCine.model.Pelicula;
import com.example.GestionDeCine.model.TicketCompra;

/**
 * Contrato de la logica de negocio relacionada a las acciones que realiza
 * un cliente: ver cartelera, comprar una entrada, ver su historial de
 * compras y ver su membresia.
 *
 * Respecto de la interfaz original: se elimino "verEntradas()" (no tenia
 * parametros, no quedaba claro de que funcion se pedian las entradas) y se
 * lo reemplazo por FuncionService.obtenerAsientosDisponibles(funcionId),
 * porque consultar la disponibilidad de asientos es una responsabilidad de
 * Funcion, no de Cliente.
 */
public interface IClienteService {

    List<TicketCompra> verHistorial(Integer usuarioId);

    Membresia verMembresia(Integer usuarioId);

    TicketCompra comprarEntrada(Integer usuarioId, Integer funcionId, Integer asientoId);

    List<Pelicula> verCartelera();
}
