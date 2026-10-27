package com.example.GestionDeCine.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GestionDeCine.dto.UsuarioRequestDTO;
import com.example.GestionDeCine.dto.UsuarioResponseDTO;
import com.example.GestionDeCine.exception.AutenticacionException;
import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.mapper.UsuarioMapper;
import com.example.GestionDeCine.model.Rol;
import com.example.GestionDeCine.model.Usuario;
import com.example.GestionDeCine.repository.TicketCompraRepository;
import com.example.GestionDeCine.repository.UsuarioRepository;
import com.example.GestionDeCine.service.interfaces.IUsuarioService;

/** Logica de negocio de usuarios: registro, login, mail y DNI unicos, borrado seguro. */
@Service
public class UsuarioService implements IUsuarioService {

    private static final String CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos";

    private final UsuarioRepository usuarioRepository;
    private final TicketCompraRepository ticketCompraRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, TicketCompraRepository ticketCompraRepository) {
        this.usuarioRepository = usuarioRepository;
        this.ticketCompraRepository = ticketCompraRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerUsuarios() {
        return usuarioRepository.findAll().stream().map(UsuarioMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerUsuarioPorId(Integer id) {
        return UsuarioMapper.toResponse(buscarUsuario(id));
    }

    /** El mismo mensaje para "no existe el mail" y "password incorrecta": no revela que mails estan registrados. */
    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO iniciarSesion(String mail, String password) {
        Usuario usuario = usuarioRepository.findByMail(mail.trim().toLowerCase())
                .orElseThrow(() -> new AutenticacionException(CREDENCIALES_INVALIDAS));
        if (!Objects.equals(usuario.getPassword(), password)) {
            throw new AutenticacionException(CREDENCIALES_INVALIDAS);
        }
        return UsuarioMapper.toResponse(usuario);
    }

    /** El rol no se toma del request: un registro publico nunca puede crear administradores. */
    @Override
    @Transactional
    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO dto) {
        validarMailYDniUnicos(dto, null);
        Usuario usuario = new Usuario();
        UsuarioMapper.copiarDatos(dto, usuario);
        usuario.setRol(Rol.CLIENTE);
        usuario.setSocio(false);
        return UsuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    /** Modifica datos personales y password; no cambia rol ni condicion de socio. */
    @Override
    @Transactional
    public UsuarioResponseDTO modificarUsuario(Integer id, UsuarioRequestDTO dto) {
        Usuario usuario = buscarUsuario(id);
        validarMailYDniUnicos(dto, usuario);
        UsuarioMapper.copiarDatos(dto, usuario);
        return UsuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    /** Un usuario con compras no se borra: se perderia el historial de ventas. Su membresia se borra en cascada (FK). */
    @Override
    @Transactional
    public void eliminarUsuario(Integer id) {
        Usuario usuario = buscarUsuario(id);
        if (ticketCompraRepository.existsByUsuarioId(id)) {
            throw new BusinessException("No se puede eliminar el usuario " + id + " porque tiene compras registradas");
        }
        usuarioRepository.delete(usuario);
    }

    /** actual es el usuario que se esta modificando (null al crear): sus propios datos no cuentan como duplicado. */
    private void validarMailYDniUnicos(UsuarioRequestDTO dto, Usuario actual) {
        String mail = dto.getMail().trim().toLowerCase();
        boolean cambiaMail = actual == null || !actual.getMail().equalsIgnoreCase(mail);
        if (cambiaMail && usuarioRepository.existsByMail(mail)) {
            throw new BusinessException("Ya existe un usuario registrado con el mail " + mail);
        }
        boolean cambiaDni = actual == null || !Objects.equals(actual.getDni(), dto.getDni());
        if (cambiaDni && usuarioRepository.existsByDni(dto.getDni())) {
            throw new BusinessException("Ya existe un usuario registrado con el DNI " + dto.getDni());
        }
    }

    private Usuario buscarUsuario(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario con id " + id));
    }
}
