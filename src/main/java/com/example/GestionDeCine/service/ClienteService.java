package com.example.GestionDeCine.service;

import com.example.GestionDeCine.model.*;
import com.example.GestionDeCine.repository.*;
import com.example.GestionDeCine.service.Interface.IClienteService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClienteService implements IClienteService {
    private final TicketCompraRepository ticketCompraRepository;
    private final MembresiaRepository membresiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FuncionRepository funcionRepository;
    private final AsientoRepository asientoRepository;
    private final EntradaRepository entradaRepository;

    public ClienteService(TicketCompraRepository ticketCompraRepository,
                          MembresiaRepository membresiaRepository,
                          UsuarioRepository usuarioRepository,
                          FuncionRepository funcionRepository,
                          AsientoRepository asientoRepository,
                          EntradaRepository entradaRepository) {
        this.ticketCompraRepository = ticketCompraRepository;
        this.membresiaRepository= membresiaRepository;
        this.usuarioRepository=usuarioRepository;
        this.funcionRepository=funcionRepository;
        this.asientoRepository=asientoRepository;
        this.entradaRepository=entradaRepository;
    }


    @Override
    public List<TicketCompra> verHistorial(Integer usuarioId) {
        return ticketCompraRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public Membresia verMembresia(Integer usuarioId) {
        return membresiaRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public TicketCompra comprarEntrada( Integer usuarioId, Integer funcionId, Integer asientoId) {



        //separar cada funcion en sus clases correspondiente

        Usuario usuario= usuarioRepository.findById( usuarioId).orElseThrow(()->
                new RuntimeException("Usuario no encontrado"));
        Funcion funcion= funcionRepository.findById(funcionId). orElseThrow(()->
                new RuntimeException("Funcion no encontrada"));

        Asiento asiento= asientoRepository.findById(asientoId).orElseThrow(()->
                new RuntimeException("Asiento no disponible"));
        if(entradaRepository.isOcupado(funcionId, asientoId)){
            throw new RuntimeException("El asiento ya esta ocupado");

        }
        Entrada entrada=new Entrada();
        entrada.setFuncion(funcion);
        entrada.setAsiento(asiento);
        entradaRepository.save(entrada);

        return null;




    }

    @Override
    public List<Pelicula> verCartelera() {
        return null;
    }

    @Override
    public List<Entrada> verEntradas() {
        return List.of();
    }
}
