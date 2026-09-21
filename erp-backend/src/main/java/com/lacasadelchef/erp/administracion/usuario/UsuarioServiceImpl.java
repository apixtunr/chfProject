package com.lacasadelchef.erp.administracion.usuario;

import com.lacasadelchef.erp.administracion.usuario.dto.CambiarPasswordRequest;
import com.lacasadelchef.erp.administracion.usuario.dto.UsuarioActualizarRequest;
import com.lacasadelchef.erp.administracion.usuario.dto.UsuarioRequest;
import com.lacasadelchef.erp.administracion.usuario.dto.UsuarioResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Rol;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.repository.EmpleadoRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.RolRepository;
import com.lacasadelchef.erp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private static final String TABLA = "usuario";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final EstadoRepository estadoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final PasswordEncoder passwordEncoder;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(String username, Pageable pageable) {
        Page<Usuario> page = (username == null || username.isBlank())
                ? usuarioRepository.findAll(pageable)
                : usuarioRepository.findByUsernameContainingIgnoreCase(username.trim(), pageable);
        return page.map(UsuarioResponse::desde);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Integer id) {
        return UsuarioResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        Usuario usuario = new Usuario();
        usuario.setUsername(request.username().trim());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setIntentosAcceso(0);
        aplicarRolEstadoEmpleado(request.idRol(), request.idEstado(), request.idEmpleado(), usuario);
        usuario = usuarioRepository.save(usuario);
        bitacoraMovimientoService.registrar(TABLA, usuario.getIdUsuario(), Operacion.INSERT);
        return UsuarioResponse.desde(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(Integer id, UsuarioActualizarRequest request) {
        Usuario usuario = buscar(id);
        aplicarRolEstadoEmpleado(request.idRol(), request.idEstado(), request.idEmpleado(), usuario);
        usuario = usuarioRepository.save(usuario);
        return UsuarioResponse.desde(usuario);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        // Si el usuario registro pagos, movimientos de inventario o bitacoras, la FK lo
        // impide y el GlobalExceptionHandler lo traduce a HTTP 409.
        Usuario usuario = buscar(id);
        usuarioRepository.delete(usuario);
        bitacoraMovimientoService.registrar(TABLA, usuario.getIdUsuario(), Operacion.DELETE);
    }

    @Override
    @Transactional
    public void cambiarPassword(Integer id, CambiarPasswordRequest request) {
        Usuario usuario = buscar(id);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setIntentosAcceso(0);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponse desbloquear(Integer id) {
        Usuario usuario = buscar(id);
        usuario.setIntentosAcceso(0);
        usuario = usuarioRepository.save(usuario);
        return UsuarioResponse.desde(usuario);
    }

    private Usuario buscar(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    private void aplicarRolEstadoEmpleado(Integer idRol, Integer idEstado, Integer idEmpleado, Usuario usuario) {
        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", idRol));
        Estado estado = estadoRepository.findById(idEstado)
                .orElseThrow(() -> new ResourceNotFoundException("Estado", idEstado));
        Empleado empleado = null;
        if (idEmpleado != null) {
            empleado = empleadoRepository.findById(idEmpleado)
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado", idEmpleado));
        }
        usuario.setRol(rol);
        usuario.setEstado(estado);
        usuario.setEmpleado(empleado);
    }
}
