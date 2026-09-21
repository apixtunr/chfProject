package com.lacasadelchef.erp.administracion.rbac;

import com.lacasadelchef.erp.administracion.rbac.dto.RolOpcionRequest;
import com.lacasadelchef.erp.administracion.rbac.dto.RolOpcionResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Opcion;
import com.lacasadelchef.erp.entity.Rol;
import com.lacasadelchef.erp.entity.RolOpcion;
import com.lacasadelchef.erp.entity.id.RolOpcionId;
import com.lacasadelchef.erp.repository.OpcionRepository;
import com.lacasadelchef.erp.repository.RolOpcionRepository;
import com.lacasadelchef.erp.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Configura, por rol, que puede hacer sobre cada opcion del menu (alta/baja/modificacion/
 * imprimir/exportar). Es la pantalla que le da datos reales a PermisoService: hasta que un
 * administrador no asigne estos permisos, los roles distintos de ADMINISTRADOR no pueden
 * ejecutar ninguna operacion de escritura en el resto de los modulos.
 */
@Service
@RequiredArgsConstructor
public class RolOpcionServiceImpl implements RolOpcionService {

    private static final String TABLA = "rol_opcion";

    private final RolOpcionRepository rolOpcionRepository;
    private final RolRepository rolRepository;
    private final OpcionRepository opcionRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<RolOpcionResponse> listar(Integer idRol) {
        return rolOpcionRepository.findPermisosByRol(idRol).stream()
                .map(RolOpcionResponse::desde)
                .toList();
    }

    @Override
    @Transactional
    public RolOpcionResponse asignar(Integer idRol, Integer idOpcion, RolOpcionRequest request) {
        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", idRol));
        Opcion opcion = opcionRepository.findById(idOpcion)
                .orElseThrow(() -> new ResourceNotFoundException("Opcion", idOpcion));

        RolOpcion rolOpcion = new RolOpcion();
        rolOpcion.setId(new RolOpcionId(idRol, idOpcion));
        rolOpcion.setRol(rol);
        rolOpcion.setOpcion(opcion);
        aplicar(request, rolOpcion);
        rolOpcion = rolOpcionRepository.save(rolOpcion);
        bitacoraMovimientoService.registrar(TABLA, idRol + "-" + idOpcion, Operacion.INSERT);
        return RolOpcionResponse.desde(rolOpcion);
    }

    @Override
    @Transactional
    public RolOpcionResponse actualizar(Integer idRol, Integer idOpcion, RolOpcionRequest request) {
        RolOpcion rolOpcion = buscar(idRol, idOpcion);
        aplicar(request, rolOpcion);
        rolOpcion = rolOpcionRepository.save(rolOpcion);
        return RolOpcionResponse.desde(rolOpcion);
    }

    @Override
    @Transactional
    public void quitar(Integer idRol, Integer idOpcion) {
        RolOpcion rolOpcion = buscar(idRol, idOpcion);
        rolOpcionRepository.delete(rolOpcion);
        bitacoraMovimientoService.registrar(TABLA, idRol + "-" + idOpcion, Operacion.DELETE);
    }

    private RolOpcion buscar(Integer idRol, Integer idOpcion) {
        return rolOpcionRepository.findById(new RolOpcionId(idRol, idOpcion))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El rol %d no tiene configurado un permiso para la opcion %d".formatted(idRol, idOpcion)));
    }

    private void aplicar(RolOpcionRequest request, RolOpcion rolOpcion) {
        rolOpcion.setAlta(request.alta());
        rolOpcion.setBaja(request.baja());
        rolOpcion.setModificacion(request.modificacion());
        rolOpcion.setImprimir(request.imprimir());
        rolOpcion.setExportar(request.exportar());
    }
}
