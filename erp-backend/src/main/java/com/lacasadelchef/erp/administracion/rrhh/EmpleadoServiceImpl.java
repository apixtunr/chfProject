package com.lacasadelchef.erp.administracion.rrhh;

import com.lacasadelchef.erp.administracion.rrhh.dto.EmpleadoRequest;
import com.lacasadelchef.erp.administracion.rrhh.dto.EmpleadoResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.administracion.rrhh.dto.AccesoSistemaRequest;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Genero;
import com.lacasadelchef.erp.entity.PuestoEmpleado;
import com.lacasadelchef.erp.entity.Rol;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.repository.EmpleadoRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.GeneroRepository;
import com.lacasadelchef.erp.repository.PuestoEmpleadoRepository;
import com.lacasadelchef.erp.repository.RolRepository;
import com.lacasadelchef.erp.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmpleadoServiceImpl implements EmpleadoService {

    private static final String TABLA = "empleado";
    private static final String TABLA_USUARIO = "usuario";
    private static final String TIPO_ESTADO_GENERAL = "GENERAL";
    private static final String ESTADO_ACTIVO = "ACTIVO";

    private final EmpleadoRepository empleadoRepository;
    private final PuestoEmpleadoRepository puestoEmpleadoRepository;
    private final EstadoRepository estadoRepository;
    private final GeneroRepository generoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public Page<EmpleadoResponse> listar(String nombre, Pageable pageable) {
        Page<Empleado> page = (nombre == null || nombre.isBlank())
                ? empleadoRepository.findAll(pageable)
                : empleadoRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(
                        nombre.trim(), nombre.trim(), pageable);
        Set<Integer> conUsuario = idsConUsuario(page.getContent());
        return page.map(e -> conUsuario.contains(e.getIdEmpleado())
                ? EmpleadoResponse.desde(e, usuarioRepository.findByEmpleadoIdEmpleado(e.getIdEmpleado()).orElse(null))
                : EmpleadoResponse.desde(e));
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponse obtenerPorId(Integer id) {
        return EmpleadoResponse.desde(buscar(id), usuarioRepository.findByEmpleadoIdEmpleado(id).orElse(null));
    }

    /**
     * Da de alta al empleado y, si se pidio, su acceso al sistema.
     *
     * Las dos cosas van en la misma transaccion (@Transactional): si la creacion del
     * usuario falla, la del empleado se deshace. Sin eso, un username repetido dejaria
     * el empleado grabado sin usuario, y al reintentar quedarian dos empleados para la
     * misma persona.
     *
     * Por eso el username se comprueba ANTES de grabar nada: es el error mas probable y
     * el que hay que atajar temprano.
     */
    @Override
    @Transactional
    public EmpleadoResponse crear(EmpleadoRequest request) {
        AccesoSistemaRequest acceso = request.acceso();
        if (acceso != null && usuarioRepository.existsByUsernameIgnoreCase(acceso.username().trim())) {
            throw new BusinessException("El usuario '" + acceso.username().trim() + "' ya existe.");
        }

        Empleado empleado = new Empleado();
        aplicar(request, empleado);
        empleado = empleadoRepository.save(empleado);
        bitacoraMovimientoService.registrar(TABLA, empleado.getIdEmpleado(), Operacion.INSERT);

        if (acceso == null) {
            return EmpleadoResponse.desde(empleado);
        }
        return EmpleadoResponse.desde(empleado, crearUsuario(empleado, acceso));
    }

    private Usuario crearUsuario(Empleado empleado, AccesoSistemaRequest acceso) {
        Rol rol = rolRepository.findById(acceso.idRol())
                .orElseThrow(() -> new ResourceNotFoundException("Rol", acceso.idRol()));
        Usuario usuario = new Usuario();
        usuario.setUsername(acceso.username().trim());
        usuario.setPasswordHash(passwordEncoder.encode(acceso.password()));
        usuario.setIntentosAcceso(0);
        usuario.setRol(rol);
        usuario.setEmpleado(empleado);
        usuario.setEstado(estadoRepository
                .findByTipoEstadoNombreTipoAndNombre(TIPO_ESTADO_GENERAL, ESTADO_ACTIVO)
                .orElseThrow(() -> new IllegalStateException("Falta el estado ACTIVO del tipo GENERAL")));
        usuario = usuarioRepository.save(usuario);
        bitacoraMovimientoService.registrar(TABLA_USUARIO, usuario.getIdUsuario(), Operacion.INSERT);
        return usuario;
    }

    /** Quienes de esta pagina tienen usuario, en una sola consulta y no una por fila. */
    private Set<Integer> idsConUsuario(List<Empleado> empleados) {
        if (empleados.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(usuarioRepository.idsEmpleadoConUsuario(
                empleados.stream().map(Empleado::getIdEmpleado).toList()));
    }

    @Override
    @Transactional
    public EmpleadoResponse actualizar(Integer id, EmpleadoRequest request) {
        if (request.acceso() != null) {
            throw new BusinessException(
                    "El acceso al sistema de un empleado que ya existe se administra desde Usuarios.");
        }
        Empleado empleado = buscar(id);
        aplicar(request, empleado);
        empleado = empleadoRepository.save(empleado);
        return EmpleadoResponse.desde(empleado, usuarioRepository.findByEmpleadoIdEmpleado(id).orElse(null));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        // Si el empleado tiene usuario, documentos o asignaciones a eventos, la FK lo
        // impide y el GlobalExceptionHandler lo traduce a HTTP 409.
        Empleado empleado = buscar(id);
        empleadoRepository.delete(empleado);
        bitacoraMovimientoService.registrar(TABLA, empleado.getIdEmpleado(), Operacion.DELETE);
    }

    private Empleado buscar(Integer id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado", id));
    }

    private void aplicar(EmpleadoRequest request, Empleado empleado) {
        PuestoEmpleado puestoEmpleado = puestoEmpleadoRepository.findById(request.idPuestoEmpleado())
                .orElseThrow(() -> new ResourceNotFoundException("PuestoEmpleado", request.idPuestoEmpleado()));
        Estado estado = estadoRepository.findById(request.idEstado())
                .orElseThrow(() -> new ResourceNotFoundException("Estado", request.idEstado()));
        Genero genero = null;
        if (request.idGenero() != null) {
            genero = generoRepository.findById(request.idGenero())
                    .orElseThrow(() -> new ResourceNotFoundException("Genero", request.idGenero()));
        }

        empleado.setPuestoEmpleado(puestoEmpleado);
        empleado.setEstado(estado);
        empleado.setGenero(genero);
        empleado.setNombre(request.nombre().trim());
        empleado.setApellido(request.apellido().trim());
        empleado.setCorreo(request.correo());
        empleado.setTelefono(request.telefono());
        empleado.setFechaContratacion(request.fechaContratacion());
    }
}
