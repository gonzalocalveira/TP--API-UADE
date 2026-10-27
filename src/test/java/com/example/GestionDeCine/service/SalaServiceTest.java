package com.example.GestionDeCine.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.GestionDeCine.dto.SalaRequestDTO;
import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.model.Sala;
import com.example.GestionDeCine.repository.AsientoRepository;
import com.example.GestionDeCine.repository.FuncionRepository;
import com.example.GestionDeCine.repository.SalaRepository;

/** Tests unitarios de las reglas de salas. */
@ExtendWith(MockitoExtension.class)
class SalaServiceTest {

    @Mock private SalaRepository salaRepository;
    @Mock private AsientoRepository asientoRepository;
    @Mock private FuncionRepository funcionRepository;

    @InjectMocks
    private SalaService salaService;

    @Test
    void crear_salaConNombreRepetido_lanzaBusinessException() {
        when(salaRepository.existsByNombreIgnoreCase("Sala 1")).thenReturn(true);

        assertThrows(BusinessException.class, () -> salaService.crearSala(new SalaRequestDTO("Sala 1", 40)));
    }

    @Test
    void modificar_capacidadMenorALasButacasCargadas_lanzaBusinessException() {
        Sala sala = new Sala();
        sala.setId(1);
        when(salaRepository.findById(1)).thenReturn(Optional.of(sala));
        when(asientoRepository.countBySalaId(1)).thenReturn(40L);

        assertThrows(BusinessException.class, () -> salaService.modificarSala(1, new SalaRequestDTO("Sala 1", 30)));
    }

    @Test
    void eliminar_salaConButacas_lanzaBusinessException() {
        when(salaRepository.findById(1)).thenReturn(Optional.of(new Sala()));
        when(asientoRepository.existsBySalaId(1)).thenReturn(true);

        assertThrows(BusinessException.class, () -> salaService.eliminarSala(1));
    }
}
