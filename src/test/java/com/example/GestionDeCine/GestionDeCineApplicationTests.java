package com.example.GestionDeCine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test minimo pero real: verifica que el contexto de Spring levanta sin
 * errores (todos los beans se pueden construir: Controllers, Services,
 * Repositories y la conexion a la base de datos). Si alguna dependencia
 * quedara mal cableada (por ejemplo, un @Autowired a una interfaz sin
 * implementacion), este test fallaria.
 */
@SpringBootTest
class GestionDeCineApplicationTests {

	@Test
	void contextLoads() {
	}

}
