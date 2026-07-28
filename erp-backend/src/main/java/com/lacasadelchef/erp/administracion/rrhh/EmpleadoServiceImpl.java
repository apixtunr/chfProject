package com.lacasadelchef.erp.administracion.rrhh;

import com.lacasadelchef.erp.administracion.rrhh.dto.EmpleadoRequest;
import com.lacasadelchef.erp.administracion.rrhh.dto.EmpleadoResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Genero;
import com.lacasadelchef.erp.entity.PuestoEmpleado;
import com.lacasadelchef.erp.repository.EmpleadoRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.GeneroRepository;
import com.lacasadelchef.erp.repository.PuestoEmpleadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmpleadoServiceImpl implements EmpleadoService {

    private static final String TABLA = "empleado";

    private final EmpleadoRepository empleadoRepository;
    private final PuestoEmpleadoRepository puestoEmpleadoRepository;
    private final EstadoRepository estadoRepository;
    private final GeneroRepository generoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public Page<EmpleadoResponse> listar(String nombre, Pageable pageable) {
        Page<Empleado> page = (nombre == null || nombre.isBlank())
                ? empleadoRepository.findAll(pageable)
                : empleadoRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(
                        nombre.trim(), nombre.trim(), pageable);
        return page.map(EmpleadoResponse::desde);
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponse obtenerPorId(Integer id) {
        return EmpleadoResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public EmpleadoResponse crear(EmpleadoRequest request) {
        Empleado empleado = new Empleado();
        aplicar(request, empleado);
        empleado = empleadoRepository.save(empleado);
        bitacoraMovimientoService.registrar(TABLA, empleado.getIdEmpleado(), Operacion.INSERT);
        return EmpleadoResponse.desde(empleado);
    }

    @Override
    @Transactional
    public EmpleadoResponse actualizar(Integer id, EmpleadoRequest request) {
        Empleado empleado = buscar(id);
        aplicar(request, empleado);
        empleado = empleadoRepository.save(empleado);
        bitacoraMovimientoService.registrar(TABLA, empleado.getIdEmpleado(), Operacion.UPDATE);
        return EmpleadoResponse.desde(empleado);
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
