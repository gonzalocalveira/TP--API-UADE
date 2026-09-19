package com.example.GestionDeCine.service.Interface;

import com.example.GestionDeCine.model.Usuario;

public interface IAdministrador {
    Usuario obtenerClientes();

    Usuario obtenerCliente(Integer id);

    Usuario crearCliente(Usuario usuario);

    void eliminarCliente(Integer id);


}
