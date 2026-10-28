package com.example.GestionDeCine.service.interfaces;

import java.util.List;

import com.example.GestionDeCine.dto.CompraRequestDTO;
import com.example.GestionDeCine.dto.MembresiaResponseDTO;
import com.example.GestionDeCine.dto.PeliculaResponseDTO;
import com.example.GestionDeCine.dto.TicketCompraResponseDTO;

/** Contrato de las acciones de un cliente: cartelera, compra, cancelacion, historial y membresia. */
public interface IClienteService {

    List<PeliculaResponseDTO> verCartelera();

    /** Compra una o mas butacas para una funcion. El total lo calcula el servidor. */
    TicketCompraResponseDTO comprarEntradas(CompraRequestDTO dto);

    /** Cancela una compra confirmada (solo antes de que empiece la funcion) y libera sus butacas. */
    TicketCompraResponseDTO cancelarCompra(Integer ticketId);

    List<TicketCompraResponseDTO> verHistorial(Integer usuarioId);

    MembresiaResponseDTO verMembresia(Integer usuarioId);
}
