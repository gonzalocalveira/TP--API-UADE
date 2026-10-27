package com.example.GestionDeCine.mapper;

import com.example.GestionDeCine.dto.EntradaResponseDTO;
import com.example.GestionDeCine.dto.TicketCompraResponseDTO;
import com.example.GestionDeCine.model.Entrada;
import com.example.GestionDeCine.model.Funcion;
import com.example.GestionDeCine.model.TicketCompra;

/** Convierte las entidades de compra (TicketCompra y sus Entradas) a sus DTOs. */
public final class CompraMapper {

    private CompraMapper() {
    }

    public static TicketCompraResponseDTO toResponse(TicketCompra ticket) {
        Funcion funcion = ticket.getFuncion();
        return new TicketCompraResponseDTO(
                ticket.getId(),
                ticket.getFechaCompra(),
                ticket.getEstado(),
                ticket.getTotal(),
                ticket.getUsuario().getId(),
                funcion.getId(),
                funcion.getPelicula().getNombre(),
                funcion.getSala().getNombre(),
                funcion.getFechaFuncion(),
                funcion.getHoraFuncion(),
                ticket.getEntradas().stream().map(CompraMapper::toResponse).toList());
    }

    public static EntradaResponseDTO toResponse(Entrada entrada) {
        return new EntradaResponseDTO(
                entrada.getId(),
                entrada.getAsiento().getId(),
                entrada.getAsiento().getFilaAsiento(),
                entrada.getAsiento().getNumeroAsiento(),
                entrada.getPrecioUnitario(),
                entrada.getEstado());
    }
}
