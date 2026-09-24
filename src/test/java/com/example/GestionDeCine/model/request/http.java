package com.example.GestionDeCine.model.request;
import Type.*;

import javax.swing.text.AbstractDocument;

import static org.aspectj.lang.reflect.DeclareAnnotation.Kind.Type;


public class http {
    POST http://localhost:8080/api/usuarios
    AbstractDocument.Content-Type: application/json

    {
        "nombreApellido": "Gonzalo Calveira",
            "dni": 12345678,
            "fechaNacimiento": "2005-03-10",
            "mail": "gonzalo@gmail.com",
            "password": "1234",
            "isSocio": true,
            "rol": "CLIENTE"
    }
}
