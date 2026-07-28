package com.lacasadelchef.erp.administracion.rrhh;

import com.lacasadelchef.erp.administracion.rrhh.dto.PuestoEmpleadoRequest;
import com.lacasadelchef.erp.administracion.rrhh.dto.PuestoEmpleadoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/puestos-empleado")
@RequiredArgsConstructor
public class PuestoEmpleadoController {

    private final PuestoEmpleadoService puestoEmpleadoService;

    @GetMapping
    public List<PuestoEmpleadoResponse> listar() {
        return puestoEmpleadoService.listar();
    }

    @GetMapping("/{id}")
    public PuestoEmpleadoResponse obtener(@PathVariable Integer id) {
        return puestoEmpleadoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/puestos-empleado', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public PuestoEmpleadoResponse crear(@Valid @RequestBody PuestoEmpleadoRequest request) {
        return puestoEmpleadoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/puestos-empleado', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public PuestoEmpleadoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody PuestoEmpleadoRequest request) {
        return puestoEmpleadoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/puestos-empleado', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        puestoEmpleadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
