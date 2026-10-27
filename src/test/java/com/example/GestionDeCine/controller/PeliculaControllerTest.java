package com.example.GestionDeCine.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.GestionDeCine.dto.PeliculaResponseDTO;
import com.example.GestionDeCine.exception.GlobalExceptionHandler;
import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.service.interfaces.IPeliculaService;

/**
 * Test de la capa controller con MockMvc: simula requests HTTP reales contra
 * PeliculaController (sin levantar Tomcat ni la base). El service es un mock,
 * asi se prueba solo lo que hace el controller: codigos HTTP, validacion y JSON.
 */
class PeliculaControllerTest {

    private IPeliculaService peliculaService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        peliculaService = mock(IPeliculaService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PeliculaController(peliculaService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void post_conDatosValidos_devuelve201ConLaPelicula() throws Exception {
        when(peliculaService.crearPelicula(any())).thenReturn(new PeliculaResponseDTO(
                7, "Interstellar", "Christopher Nolan", 169, LocalDate.of(2014, 11, 6),
                "Ingles", "Ciencia ficcion", "+13", "Viaje por un agujero de gusano."));

        mockMvc.perform(post("/api/peliculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Interstellar","director":"Christopher Nolan","duracion":169,
                                 "fechaEstreno":"2014-11-06","idioma":"Ingles","genero":"Ciencia ficcion",
                                 "clasificacionEdad":"+13","sinopsis":"Viaje por un agujero de gusano."}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nombre").value("Interstellar"));
    }

    @Test
    void post_conDatosInvalidos_devuelve400ConElDetalle() throws Exception {
        mockMvc.perform(post("/api/peliculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"duracion\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detalles").isArray());
    }

    @Test
    void get_peliculaInexistente_devuelve404() throws Exception {
        when(peliculaService.obtenerPeliculaPorId(99)).thenThrow(new ResourceNotFoundException("No existe la pelicula con id 99"));

        mockMvc.perform(get("/api/peliculas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe la pelicula con id 99"))
                .andExpect(jsonPath("$.path").value("/api/peliculas/99"));
    }
}
