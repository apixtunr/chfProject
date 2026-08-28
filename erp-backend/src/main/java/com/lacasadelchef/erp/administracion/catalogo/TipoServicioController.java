package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.TipoServicioRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.TipoServicioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-servicio")
@RequiredArgsConstructor
public class TipoServicioController {

    private final TipoServicioService tipoServicioService;

    @GetMapping
    public List<TipoServicioResponse> listar() {
        return tipoServicioService.listar();
    }

    @GetMapping("/{id}")
    public TipoServicioResponse obtener(@PathVariable Integer id) {
        return tipoServicioService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-servicio', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public TipoServicioResponse crear(@Valid @RequestBody TipoServicioRequest request) {
        return tipoServicioService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-servicio', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public TipoServicioResponse actualizar(@PathVariable Integer id, @Valid @RequestBody TipoServicioRequest request) {
        return tipoServicioService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-servicio', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        tipoServicioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
