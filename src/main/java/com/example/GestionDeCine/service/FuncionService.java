package com.example.GestionDeCine.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GestionDeCine.dto.AsientoResponseDTO;
import com.example.GestionDeCine.dto.ButacaDTO;
import com.example.GestionDeCine.dto.FuncionRequestDTO;
import com.example.GestionDeCine.dto.FuncionResponseDTO;
import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.mapper.AsientoMapper;
import com.example.GestionDeCine.mapper.FuncionMapper;
import com.example.GestionDeCine.model.Asiento;
import com.example.GestionDeCine.model.EstadoEntrada;
import com.example.GestionDeCine.model.Funcion;
import com.example.GestionDeCine.model.Pelicula;
import com.example.GestionDeCine.model.Sala;
import com.example.GestionDeCine.repository.AsientoRepository;
import com.example.GestionDeCine.repository.EntradaRepository;
import com.example.GestionDeCine.repository.FuncionRepository;
import com.example.GestionDeCine.repository.PeliculaRepository;
import com.example.GestionDeCine.repository.SalaRepository;
import com.example.GestionDeCine.repository.TicketCompraRepository;
import com.example.GestionDeCine.service.interfaces.IFuncionService;

/**
 * Logica de negocio de funciones: programacion sin superposiciones en una
 * misma sala y disponibilidad de butacas por funcion.
 */
@Service
public class FuncionService implements IFuncionService {

    private final FuncionRepository funcionRepository;
    private final PeliculaRepository peliculaRepository;
    private final SalaRepository salaRepository;
    private final AsientoRepository asientoRepository;
    private final EntradaRepository entradaRepository;
    private final TicketCompraRepository ticketCompraRepository;

    public FuncionService(FuncionRepository funcionRepository, PeliculaRepository peliculaRepository,
                          SalaRepository salaRepository, AsientoRepository asientoRepository,
                          EntradaRepository entradaRepository, TicketCompraRepository ticketCompraRepository) {
        this.funcionRepository = funcionRepository;
        this.peliculaRepository = peliculaRepository;
        this.salaRepository = salaRepository;
        this.asientoRepository = asientoRepository;
        this.entradaRepository = entradaRepository;
        this.ticketCompraRepository = ticketCompraRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuncionResponseDTO> obtenerFunciones(Integer peliculaId, LocalDate fecha) {
        return funcionRepository.buscar(peliculaId, fecha).stream().map(FuncionMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FuncionResponseDTO obtenerFuncionPorId(Integer id) {
        return FuncionMapper.toResponse(buscarFuncion(id));
    }

    @Override
    @Transactional
    public FuncionResponseDTO crearFuncion(FuncionRequestDTO dto) {
        Funcion funcion = new Funcion();
        validarYCopiar(dto, funcion, null);
        return FuncionMapper.toResponse(funcionRepository.save(funcion));
    }

    /** Una funcion con entradas vendidas no se modifica: cambiaria lo que el cliente ya compro. */
    @Override
    @Transactional
    public FuncionResponseDTO modificarFuncion(Integer id, FuncionRequestDTO dto) {
        Funcion funcion = buscarFuncion(id);
        if (ticketCompraRepository.existsByFuncionId(id)) {
            throw new BusinessException("No se puede modificar la funcion " + id + " porque ya tiene compras");
        }
        validarYCopiar(dto, funcion, id);
        return FuncionMapper.toResponse(funcionRepository.save(funcion));
    }

    @Override
    @Transactional
    public void eliminarFuncion(Integer id) {
        Funcion funcion = buscarFuncion(id);
        if (ticketCompraRepository.existsByFuncionId(id)) {
            throw new BusinessException("No se puede eliminar la funcion " + id + " porque ya tiene compras");
        }
        funcionRepository.delete(funcion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ButacaDTO> obtenerButacas(Integer funcionId) {
        Funcion funcion = buscarFuncion(funcionId);
        Set<Integer> ocupadas = idsOcupados(funcionId);
        return butacasDeLaSala(funcion).stream()
                .map(asiento -> AsientoMapper.toButaca(asiento, ocupadas.contains(asiento.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsientoResponseDTO> obtenerAsientosDisponibles(Integer funcionId) {
        Funcion funcion = buscarFuncion(funcionId);
        Set<Integer> ocupadas = idsOcupados(funcionId);
        return butacasDeLaSala(funcion).stream()
                .filter(asiento -> !ocupadas.contains(asiento.getId()))
                .map(AsientoMapper::toResponse)
                .toList();
    }

    /**
     * Aplica las reglas de programacion y, si pasan, copia los datos a la entidad.
     * idExcluido es la funcion que se esta modificando (para no compararla consigo misma).
     */
    private void validarYCopiar(FuncionRequestDTO dto, Funcion funcion, Integer idExcluido) {
        Pelicula pelicula = peliculaRepository.findById(dto.getPeliculaId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe la pelicula con id " + dto.getPeliculaId()));
        Sala sala = salaRepository.findById(dto.getSalaId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe la sala con id " + dto.getSalaId()));

        LocalDateTime inicio = LocalDateTime.of(dto.getFechaFuncion(), dto.getHoraFuncion());
        if (!inicio.isAfter(LocalDateTime.now())) {
            throw new BusinessException("La funcion debe programarse para una fecha y hora futura");
        }
        validarSinSuperposicion(sala, inicio, inicio.plusMinutes(pelicula.getDuracion()), idExcluido);

        funcion.setFechaFuncion(dto.getFechaFuncion());
        funcion.setHoraFuncion(dto.getHoraFuncion());
        funcion.setFormatoFuncion(dto.getFormatoFuncion());
        funcion.setPrecio(dto.getPrecio());
        funcion.setPelicula(pelicula);
        funcion.setSala(sala);
    }

    /**
     * Dos funciones de la misma sala se superponen si una empieza antes de que termine la otra:
     * inicioA < finB  y  inicioB < finA  (el fin es inicio + duracion de la pelicula).
     */
    private void validarSinSuperposicion(Sala sala, LocalDateTime inicio, LocalDateTime fin, Integer idExcluido) {
        // Se miran tambien el dia anterior y el siguiente: una funcion de la noche puede terminar pasada la medianoche.
        LocalDate dia = inicio.toLocalDate();
        List<Funcion> delMismoRango = funcionRepository.findBySalaEntreFechas(sala.getId(), dia.minusDays(1), dia.plusDays(1));
        for (Funcion otra : delMismoRango) {
            if (Objects.equals(otra.getId(), idExcluido)) {
                continue;
            }
            LocalDateTime inicioOtra = otra.getInicio();
            LocalDateTime finOtra = inicioOtra.plusMinutes(otra.getPelicula().getDuracion());
            if (inicio.isBefore(finOtra) && inicioOtra.isBefore(fin)) {
                throw new BusinessException("La funcion se superpone en " + sala.getNombre() + " con la funcion " + otra.getId()
                        + " (" + otra.getPelicula().getNombre() + ", de " + inicioOtra.toLocalTime() + " a " + finOtra.toLocalTime() + ")");
            }
        }
    }

    private List<Asiento> butacasDeLaSala(Funcion funcion) {
        return asientoRepository.findBySalaIdOrderByFilaAsientoAscNumeroAsientoAsc(funcion.getSala().getId());
    }

    private Set<Integer> idsOcupados(Integer funcionId) {
        return new HashSet<>(entradaRepository.findAsientoIdsByFuncionIdAndEstado(funcionId, EstadoEntrada.OCUPADA));
    }

    private Funcion buscarFuncion(Integer id) {
        return funcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la funcion con id " + id));
    }
}
