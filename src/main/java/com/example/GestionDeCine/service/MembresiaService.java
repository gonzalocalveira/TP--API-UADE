package com.example.GestionDeCine.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GestionDeCine.dto.MembresiaRequestDTO;
import com.example.GestionDeCine.dto.MembresiaResponseDTO;
import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.mapper.MembresiaMapper;
import com.example.GestionDeCine.model.Membresia;
import com.example.GestionDeCine.model.Usuario;
import com.example.GestionDeCine.repository.MembresiaRepository;
import com.example.GestionDeCine.repository.UsuarioRepository;
import com.example.GestionDeCine.service.interfaces.IMembresiaService;

/**
 * Logica de negocio del programa de socios. Antes esta logica estaba en
 * MembresiaController; se movio aca para que el controller solo maneje HTTP.
 */
@Service
public class MembresiaService implements IMembresiaService {

    private final MembresiaRepository membresiaRepository;
    private final UsuarioRepository usuarioRepository;

    public MembresiaService(MembresiaRepository membresiaRepository, UsuarioRepository usuarioRepository) {
        this.membresiaRepository = membresiaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** Modifica dos tablas (membresia y usuarios.is_socio): por eso es una sola transaccion. */
    @Override
    @Transactional
    public MembresiaResponseDTO crearMembresia(MembresiaRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario con id " + dto.getUsuarioId()));
        if (membresiaRepository.existsByUsuarioId(usuario.getId())) {
            throw new BusinessException("El usuario " + usuario.getId() + " ya tiene una membresia");
        }

        Membresia membresia = new Membresia();
        membresia.setUsuario(usuario);
        membresia.setPuntos(dto.getPuntos());
        membresia.setTipoMembresia(dto.getTipoMembresia());
        usuario.setSocio(true);
        return MembresiaMapper.toResponse(membresiaRepository.save(membresia));
    }

    @Override
    @Transactional
    public void eliminarMembresia(Integer id) {
        Membresia membresia = membresiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la membresia con id " + id));
        membresia.getUsuario().setSocio(false);
        membresiaRepository.delete(membresia);
    }
}
