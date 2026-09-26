package com.lacasadelchef.erp.administracion.rrhh;

import com.lacasadelchef.erp.administracion.rrhh.dto.EmpleadoRequest;
import com.lacasadelchef.erp.administracion.rrhh.dto.EmpleadoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    @GetMapping
    public Page<EmpleadoResponse> listar(@RequestParam(required = false) String nombre,
                                         @PageableDefault(size = 20, sort = "apellido") Pageable pageable) {
        return empleadoService.listar(nombre, pageable);
    }

    @GetMapping("/{id}")
    public EmpleadoResponse obtener(@PathVariable Integer id) {
        return empleadoService.obtenerPorId(id);
    }

    /**
     * Alta de empleado. Si el cuerpo trae el bloque "acceso", tambien se le crea el
     * usuario, en la misma transaccion.
     *
     * Por eso se piden dos permisos: dar de alta empleados siempre, y dar de alta
     * usuarios solo cuando se pidio el acceso. Hoy ningun rol fuera de ADMINISTRADOR
     * tiene configurados esos modulos, pero si manana se le da Empleados a otro rol, no
     * se lleva de regalo la capacidad de crear usuarios.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/empleados', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)"
            + " and (#request.acceso() == null"
            + " or @permisoService.tienePermiso('/api/usuarios', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA))")
    public EmpleadoResponse crear(@Valid @RequestBody EmpleadoRequest request) {
        return empleadoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/empleados', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public EmpleadoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody EmpleadoRequest request) {
        return empleadoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/empleados', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        empleadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
