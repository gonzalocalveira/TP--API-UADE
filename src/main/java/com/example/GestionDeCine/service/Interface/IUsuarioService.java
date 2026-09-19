package com.example.GestionDeCine.service.Interface;

import com.example.GestionDeCine.model.Usuario;

public interface IUsuarioService {

    Usuario iniciarSesion(String mail, String password);
    boolean tienePermiso( Usuario usuario, String operacion);

}
