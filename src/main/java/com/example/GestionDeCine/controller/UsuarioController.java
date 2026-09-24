package com.example.GestionDeCine.controller;


import com.example.GestionDeCine.model.Usuario;
import com.example.GestionDeCine.service.Interface.IUsuarioService;
import lombok.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final IUsuarioService iUsuarioService;

    private Usuario crearUsuario(@RequestBody Usuario usuario){
        return iUsuarioService.crearUsuario(usuario);
    }



}
