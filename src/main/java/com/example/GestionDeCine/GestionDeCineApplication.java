package com.example.GestionDeCine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicacion.
 *
 * @SpringBootApplication combina tres anotaciones:
 *  - @Configuration: esta clase puede definir beans de configuracion.
 *  - @EnableAutoConfiguration: Spring Boot configura automaticamente el
 *    servidor web embebido (Tomcat), el DataSource, JPA, etc. en base a las
 *    dependencias que encuentra en el pom.xml.
 *  - @ComponentScan: escanea el paquete com.example.GestionDeCine (y
 *    subpaquetes) buscando @Component/@Service/@Repository/@RestController
 *    para registrarlos como beans.
 */
@SpringBootApplication
public class GestionDeCineApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionDeCineApplication.class, args);
	}

}
