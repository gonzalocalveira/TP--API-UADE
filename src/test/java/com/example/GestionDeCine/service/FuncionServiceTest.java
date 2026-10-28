package com.example.GestionDeCine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.GestionDeCine.dto.FuncionRequestDTO;
import com.example.GestionDeCine.dto.FuncionResponseDTO;
import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.model.FormatoFuncion;
import com.example.GestionDeCine.model.Funcion;
import com.example.GestionDeCine.model.Pelicula;
import com.example.GestionDeCine.model.Sala;
import com.example.GestionDeCine.repository.AsientoRepository;
import com.example.GestionDeCine.repository.EntradaRepository;
import com.example.GestionDeCine.repository.FuncionRepository;
import com.example.GestionDeCine.repository.PeliculaRepository;
import com.example.GestionDeCine.repository.SalaRepository;
import com.example.GestionDeCine.repository.TicketCompraRepository;

/** Tests unitarios de la regla "no hay dos funciones superpuestas en la misma sala". */
@ExtendWith(MockitoExtension.class)
class FuncionServiceTest {

    @Mock private FuncionRepository funcionRepository;
    @Mock private PeliculaRepository peliculaRepository;
    @Mock private SalaRepository salaRepository;
    @Mock private AsientoRepository asientoRepository;
    @Mock private EntradaRepository entradaRepository;
    @Mock private TicketCompraRepository ticketCompraRepository;

    @InjectMocks
    private FuncionService funcionService;

    private final LocalDate manana = LocalDate.now().plusDays(1);
    private Pelicula pelicula;
    private Sala sala;

    @BeforeEach
    void setUp() {
        pelicula = new Pelicula();
        pelicula.setId(1);
        pelicula.setNombre("Interstellar");
        pelicula.setDuracion(120);

        sala = new Sala();
        sala.setId(1);
        sala.setNombre("Sala 1");
    }

    @Test
    void crear_funcionQueSeSuperponeConOtra_lanzaBusinessException() {
        stubPeliculaYSala();
        // Ya hay una funcion de 18:00 a 20:00; la nueva empieza 19:00.
        when(funcionRepository.findBySalaEntreFechas(anyInt(), any(), any())).thenReturn(List.of(funcionExistente(18, 0)));

        assertThrows(BusinessException.class, () -> funcionService.crearFuncion(request(19, 0)));
    }

    @Test
    void crear_funcionQueEmpiezaJustoCuandoTerminaLaOtra_seCrea() {
        stubPeliculaYSala();
        when(funcionRepository.findBySalaEntreFechas(anyInt(), any(), any())).thenReturn(List.of(funcionExistente(18, 0)));
        when(funcionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FuncionResponseDTO creada = funcionService.crearFuncion(request(20, 0));

        assertEquals(LocalTime.of(20, 0), creada.getHoraFuncion());
    }

    @Test
    void crear_funcionEnElPasado_lanzaBusinessException() {
        stubPeliculaYSala();
        FuncionRequestDTO dto = request(10, 0);
        dto.setFechaFuncion(LocalDate.now().minusDays(1));

        assertThrows(BusinessException.class, () -> funcionService.crearFuncion(dto));
    }

    private void stubPeliculaYSala() {
        when(peliculaRepository.findById(1)).thenReturn(Optional.of(pelicula));
        when(salaRepository.findById(1)).thenReturn(Optional.of(sala));
    }

    private Funcion funcionExistente(int hora, int minuto) {
        Funcion f = new Funcion();
        f.setId(99);
        f.setPelicula(pelicula);
        f.setSala(sala);
        f.setFechaFuncion(manana);
        f.setHoraFuncion(LocalTime.of(hora, minuto));
        return f;
    }

    private FuncionRequestDTO request(int hora, int minuto) {
        return new FuncionRequestDTO(manana, LocalTime.of(hora, minuto), FormatoFuncion.DOS_D,
                new BigDecimal("4000.00"), 1, 1);
    }
}
