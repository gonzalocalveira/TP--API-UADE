
package com.example.GestionDeCine.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GestionDeCine.exception.AutenticacionException;
import com.example.GestionDeCine.exception.BusinessException;
import com.example.GestionDeCine.exception.ResourceNotFoundException;
import com.example.GestionDeCine.model.Rol;
import com.example.GestionDeCine.model.Usuario;
import com.example.GestionDeCine.repository.UsuarioRepository;
import com.example.GestionDeCine.service.interfaces.IUsuarioService;

/**
 * Implementacion de la logica de negocio de Usuario.
 *
 * Responsabilidades:
 * - Consultar usuarios.
 * - Registrar usuarios.
 * - Modificar datos del perfil.
 * - Eliminar usuarios.
 * - Validar credenciales.
 * - Gestionar permisos segun el rol.
 * Las operaciones administrativas sobre roles deben realizarse exclusivamente desde flujos protegidos de Administrador.
 * La password se guarda en texto plano
 */

@Service
public class UsuarioService implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // =========================================================
    // CONSULTAS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario no encontrado con id " + id
                        )
                );
    }

    // =========================================================
    // AUTENTICACION
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Usuario iniciarSesion(String mail, String password) {

        if (mail == null || mail.isBlank()
                || password == null || password.isBlank()) {
            throw new AutenticacionException(
                    "Usuario o contraseña incorrectos"
            );
        }

        Usuario usuario = usuarioRepository.findByMail(mail)
                .orElseThrow(() ->
                        new AutenticacionException(
                                "Usuario o contraseña incorrectos"
                        )
                );

        // Comparacion correspondiente al alcance actual del TPO.
        // Si se utiliza BCrypt, esta comparacion debe reemplazarse
        // por passwordEncoder.matches(password, usuario.getPassword()).
        if (!Objects.equals(usuario.getPassword(), password)) {
            throw new AutenticacionException(
                    "Usuario o contraseña incorrectos"
            );
        }

        return usuario;
    }

    // =========================================================
    // CREACION DE USUARIOS
    // =========================================================

    /**
     * Registra un usuario como CLIENTE.
     *
     * El rol no se toma del JSON recibido, para evitar que un
     * registro publico pueda crear administradores.
     *
     * La condicion de socio se administra por separado.
     */
    @Override
    @Transactional
    public Usuario crearUsuario(Usuario usuario) {

        validarUsuarioNoNulo(usuario);
        validarDuplicadosEnCreacion(usuario);

        usuario.setRol(Rol.CLIENTE);
        usuario.setSocio(false);

        return usuarioRepository.save(usuario);
    }

    /**
     * Permite registrar un usuario con el rol indicado desde
     * un flujo administrativo previamente autorizado.
     *
     * Este metodo no autentica por si mismo al administrador.
     * Debe invocarse exclusivamente desde una operacion protegida
     * de AdministradorService o su controlador autorizado.
     */
    @Transactional
    public Usuario crearUsuarioAdministrativo(Usuario usuario) {

        validarUsuarioNoNulo(usuario);
        validarDuplicadosEnCreacion(usuario);

        if (usuario.getRol() == null) {
            throw new BusinessException(
                    "El rol del usuario es obligatorio"
            );
        }

        // La condicion de socio no se asigna desde el registro.
        usuario.setSocio(false);

        return usuarioRepository.save(usuario);
    }

    // =========================================================
    // MODIFICACION DEL PERFIL
    // =========================================================

    /**
     * Modifica los datos generales del usuario.
     *
     * No modifica password, rol ni condicion de socio:
     * esas responsabilidades deben gestionarse por operaciones
     * especificas y autorizadas.
     */
    @Override
    @Transactional
    public Usuario modificarUsuario(Integer id, Usuario usuario) {

        validarUsuarioNoNulo(usuario);

        Usuario usuarioExistente = obtenerUsuarioPorId(id);

        // Validar que el nuevo mail no pertenezca a otro usuario.
        if (!Objects.equals(
                usuarioExistente.getMail(),
                usuario.getMail()
        ) && usuarioRepository.existsByMail(usuario.getMail())) {

            throw new BusinessException(
                    "Ya existe un usuario registrado con el mail "
                            + usuario.getMail()
            );
        }

        // Validar que el nuevo DNI no pertenezca a otro usuario.
        if (!Objects.equals(
                usuarioExistente.getDni(),
                usuario.getDni()
        ) && usuarioRepository.existsByDni(usuario.getDni())) {

            throw new BusinessException(
                    "Ya existe un usuario registrado con el DNI "
                            + usuario.getDni()
            );
        }

        usuarioExistente.setNombreApellido(
                usuario.getNombreApellido()
        );

        usuarioExistente.setDni(usuario.getDni());

        usuarioExistente.setFechaNacimiento(
                usuario.getFechaNacimiento()
        );

        usuarioExistente.setMail(usuario.getMail());

        return usuarioRepository.save(usuarioExistente);
    }

    // =========================================================
    // CAMBIO DE PASSWORD
    // =========================================================

    /**
     * Cambia la password de un usuario.
     *
     * La autorizacion para modificar la password de una cuenta
     * debe controlarse en el flujo que invoque este metodo.
     */
    @Transactional
    public Usuario cambiarPassword(Integer id, String nuevaPassword) {

        if (nuevaPassword == null || nuevaPassword.isBlank()) {
            throw new BusinessException(
                    "La password es obligatoria"
            );
        }

        if (nuevaPassword.length() < 4) {
            throw new BusinessException(
                    "La password debe tener al menos 4 caracteres"
            );
        }

        Usuario usuario = obtenerUsuarioPorId(id);

        usuario.setPassword(nuevaPassword);

        return usuarioRepository.save(usuario);
    }

    // =========================================================
    // MODIFICACION ADMINISTRATIVA DEL ROL
    // =========================================================

    /**
     * Cambia el rol de un usuario desde un flujo administrativo
     * previamente autorizado.
     *
     * No debe exponerse como una operacion publica sin controles
     * de autorizacion.
     */
    @Transactional
    public Usuario cambiarRolAdministrativamente(
            Integer id,
            Rol nuevoRol
    ) {

        if (nuevoRol == null) {
            throw new BusinessException(
                    "El rol es obligatorio"
            );
        }

        Usuario usuario = obtenerUsuarioPorId(id);

        usuario.setRol(nuevoRol);

        return usuarioRepository.save(usuario);
    }

    // =========================================================
    // ELIMINACION
    // =========================================================

    @Override
    @Transactional
    public void eliminarUsuario(Integer id) {

        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Usuario no encontrado con id " + id
            );
        }

        usuarioRepository.deleteById(id);
    }

    // =========================================================
    // PERMISOS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public boolean tienePermiso(Usuario usuario, String operacion) {

        if (usuario == null
                || usuario.getRol() == null
                || operacion == null) {
            return false;
        }

        return switch (usuario.getRol()) {

            case CLIENTE ->
                    operacion.equals("VER_CARTELERA")
                            || operacion.equals("COMPRAR_ENTRADA")
                            || operacion.equals("VER_HISTORIAL")
                            || operacion.equals("VER_MEMBRESIA");

            case EMPLEADO ->
                    operacion.equals("VER_ASIENTOS")
                            || operacion.equals("ASIGNAR_ASIENTO")
                            || operacion.equals("VER_SALAS");

            case ADMINISTRADOR -> true;
        };
    }

    // =========================================================
    // VALIDACIONES PRIVADAS
    // =========================================================

    private void validarUsuarioNoNulo(Usuario usuario) {

        if (usuario == null) {
            throw new BusinessException(
                    "Los datos del usuario son obligatorios"
            );
        }
    }

    private void validarDuplicadosEnCreacion(Usuario usuario) {

        if (usuarioRepository.existsByMail(usuario.getMail())) {
            throw new BusinessException(
                    "Ya existe un usuario registrado con el mail "
                            + usuario.getMail()
            );
        }

        if (usuarioRepository.existsByDni(usuario.getDni())) {
            throw new BusinessException(
                    "Ya existe un usuario registrado con el DNI "
                            + usuario.getDni()
            );
        }
    }
}
