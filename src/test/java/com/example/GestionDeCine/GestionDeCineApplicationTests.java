package com.example.GestionDeCine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Levanta el contexto completo de Spring contra la base MySQL "gestioncine" ya
 * creada con database/01_schema.sql. Como ddl-auto=validate, este test falla si
 * alguna @Entity no coincide con las tablas del script, o si algun bean quedo
 * mal cableado. Requiere MySQL corriendo y la variable DB_PASSWORD.
 */
@SpringBootTest
class GestionDeCineApplicationTests {

	@Test
	void contextLoads() {
	}

}
