package com.lacasadelchef.erp.administracion.rrhh;

import com.lacasadelchef.erp.administracion.rrhh.dto.EmpleadoRequest;
import com.lacasadelchef.erp.administracion.rrhh.dto.EmpleadoResponse;
import com.lacasadelchef.erp.administracion.rrhh.dto.AccesoSistemaRequest;
import com.lacasadelchef.erp.administracion.rrhh.dto.DocumentoEmpleadoResponse;
import com.lacasadelchef.erp.administracion.rrhh.dto.DocumentoItemRequest;
import com.lacasadelchef.erp.administracion.usuario.PoliticaUsuario;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.DocumentoEmpleado;
import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Genero;
import com.lacasadelchef.erp.entity.PuestoEmpleado;
import com.lacasadelchef.erp.entity.Rol;
import com.lacasadelchef.erp.entity.TipoDocumento;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.repository.DocumentoEmpleadoRepository;
import com.lacasadelchef.erp.repository.EmpleadoRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.GeneroRepository;
import com.lacasadelchef.erp.repository.PuestoEmpleadoRepository;
import com.lacasadelchef.erp.repository.RolRepository;
import com.lacasadelchef.erp.repository.TipoDocumentoRepository;
import com.lacasadelchef.erp.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmpleadoServiceImpl implements EmpleadoService {

    private static final String TIPO_ESTADO_GENERAL = "GENERAL";
    private static final String ESTADO_ACTIVO = "ACTIVO";

    private final EmpleadoRepository empleadoRepository;
    private final PuestoEmpleadoRepository puestoEmpleadoRepository;
    private final EstadoRepository estadoRepository;
    private final GeneroRepository generoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final PoliticaUsuario politicaUsuario;
    private final DocumentoEmpleadoRepository documentoEmpleadoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final DocumentosEmpleado documentosEmpleado;

    @Override
    @Transactional(readOnly = true)
    public Page<EmpleadoResponse> listar(String nombre, Pageable pageable) {
        Page<Empleado> page = (nombre == null || nombre.isBlank())
                ? empleadoRepository.findAll(pageable)
                : empleadoRepository.buscar(nombre.trim(), nombre.replaceAll("[\\s-]", ""),
                        DocumentosEmpleado.TIPO_DPI, pageable);
        Set<Integer> conUsuario = idsConUsuario(page.getContent());
        Map<Integer, String> dpis = dpisDe(page.getContent());
        return page.map(e -> EmpleadoResponse.desde(e,
                conUsuario.contains(e.getIdEmpleado())
                        ? usuarioRepository.findByEmpleadoIdEmpleado(e.getIdEmpleado()).orElse(null)
                        : null,
                dpis.get(e.getIdEmpleado()), null));
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponse obtenerPorId(Integer id) {
        return respuestaCompleta(buscar(id));
    }

    /**
     * Da de alta al empleado y, si se pidio, su acceso al sistema.
     *
     * Las dos cosas van en la misma transaccion (@Transactional): si la creacion del
     * usuario falla, la del empleado se deshace. Sin eso, un error al crear el usuario
     * dejaria el empleado grabado sin usuario, y al reintentar quedarian dos empleados
     * para la misma persona. El nombre de usuario lo genera PoliticaUsuario con el nombre
     * del empleado, asi que no puede chocar con otro.
     */
    @Override
    @Transactional
    public EmpleadoResponse crear(EmpleadoRequest request) {
        AccesoSistemaRequest acceso = request.acceso();

        Empleado empleado = new Empleado();
        aplicar(request, empleado);
        empleado = empleadoRepository.save(empleado);
        guardarDocumentos(empleado, request);

        if (acceso != null) {
            crearUsuario(empleado, acceso);
        }
        return respuestaCompleta(empleado);
    }

    private Usuario crearUsuario(Empleado empleado, AccesoSistemaRequest acceso) {
        Rol rol = rolRepository.findById(acceso.idRol())
                .orElseThrow(() -> new ResourceNotFoundException("Rol", acceso.idRol()));
        Usuario usuario = new Usuario();
        usuario.setUsername(politicaUsuario.generarPara(empleado));
        usuario.setPasswordHash(passwordEncoder.encode(acceso.password()));
        usuario.setIntentosAcceso(0);
        usuario.setRol(rol);
        usuario.setEmpleado(empleado);
        usuario.setEstado(estadoRepository
                .findByTipoEstadoNombreTipoAndNombre(TIPO_ESTADO_GENERAL, ESTADO_ACTIVO)
                .orElseThrow(() -> new IllegalStateException("Falta el estado ACTIVO del tipo GENERAL")));
        usuario = usuarioRepository.save(usuario);
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
        guardarDocumentos(empleado, request);
        return respuestaCompleta(empleado);
    }

    /**
     * Guarda el DPI y los demas documentos junto con los datos del empleado, en la misma
     * transaccion: si un documento esta repetido o mal escrito, no queda nada a medias.
     */
    private void guardarDocumentos(Empleado empleado, EmpleadoRequest request) {
        TipoDocumento tipoDpi = documentosEmpleado.tipoDpi();
        documentosEmpleado.guardar(empleado, tipoDpi, request.dpi());
        if (request.documentos() == null) {
            return;
        }

        Set<Integer> enviados = new HashSet<>();
        for (DocumentoItemRequest item : request.documentos()) {
            TipoDocumento tipo = tipoDocumentoRepository.findById(item.idTipoDocumento())
                    .orElseThrow(() -> new ResourceNotFoundException("TipoDocumento", item.idTipoDocumento()));
            if (DocumentosEmpleado.esDpi(tipo)) {
                throw new BusinessException("El DPI se registra en los datos del empleado, no en sus documentos");
            }
            if (!enviados.add(tipo.getIdTipoDocumento())) {
                throw new BusinessException("El documento %s viene repetido".formatted(tipo.getNombreTipo()));
            }
            documentosEmpleado.guardar(empleado, tipo, item.numeroDocumento());
        }
        // La lista reemplaza a lo registrado: lo que ya no viene se quita (el DPI nunca).
        documentoEmpleadoRepository.findByEmpleadoIdEmpleado(empleado.getIdEmpleado()).stream()
                .filter(d -> !DocumentosEmpleado.esDpi(d.getTipoDocumento()))
                .filter(d -> !enviados.contains(d.getTipoDocumento().getIdTipoDocumento()))
                .forEach(documentoEmpleadoRepository::delete);
    }

    private EmpleadoResponse respuestaCompleta(Empleado empleado) {
        List<DocumentoEmpleado> documentos =
                documentoEmpleadoRepository.findByEmpleadoIdEmpleado(empleado.getIdEmpleado());
        String dpi = documentos.stream()
                .filter(d -> DocumentosEmpleado.esDpi(d.getTipoDocumento()))
                .map(DocumentoEmpleado::getNumeroDocumento)
                .findFirst().orElse(null);
        List<DocumentoEmpleadoResponse> otros = documentos.stream()
                .filter(d -> !DocumentosEmpleado.esDpi(d.getTipoDocumento()))
                .map(DocumentoEmpleadoResponse::desde)
                .toList();
        return EmpleadoResponse.desde(empleado,
                usuarioRepository.findByEmpleadoIdEmpleado(empleado.getIdEmpleado()).orElse(null), dpi, otros);
    }

    /** El DPI de los empleados de esta pagina, en una sola consulta. */
    private Map<Integer, String> dpisDe(List<Empleado> empleados) {
        if (empleados.isEmpty()) {
            return Map.of();
        }
        return documentoEmpleadoRepository.findByEmpleadoIdEmpleadoInAndTipoDocumentoNombreTipo(
                        empleados.stream().map(Empleado::getIdEmpleado).toList(), DocumentosEmpleado.TIPO_DPI)
                .stream()
                .collect(Collectors.toMap(d -> d.getEmpleado().getIdEmpleado(), DocumentoEmpleado::getNumeroDocumento));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        // Si el empleado tiene usuario, documentos o asignaciones a eventos, la FK lo
        // impide y el GlobalExceptionHandler lo traduce a HTTP 409.
        Empleado empleado = buscar(id);
        empleadoRepository.delete(empleado);
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
