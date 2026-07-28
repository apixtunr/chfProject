package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.cotizacion.dto.CotizacionRequest;
import com.lacasadelchef.erp.cotizacion.dto.CotizacionResponse;
import com.lacasadelchef.erp.cotizacion.dto.CotizacionVersionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cotizaciones")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService cotizacionService;
    private final CotizacionVersionService cotizacionVersionService;

    @GetMapping
    public Page<CotizacionResponse> listar(@RequestParam(required = false) Integer idCliente,
                                           @PageableDefault(size = 20, sort = "fechaCotizacion") Pageable pageable) {
        return cotizacionService.listar(idCliente, pageable);
    }

    @GetMapping("/{id}")
    public CotizacionResponse obtener(@PathVariable Integer id) {
        return cotizacionService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/cotizaciones', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public CotizacionResponse crear(@Valid @RequestBody CotizacionRequest request) {
        return cotizacionService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/cotizaciones', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public CotizacionResponse actualizar(@PathVariable Integer id, @Valid @RequestBody CotizacionRequest request) {
        return cotizacionService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/cotizaciones', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        cotizacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/versiones")
    public List<CotizacionVersionResponse> listarVersiones(@PathVariable Integer id) {
        return cotizacionVersionService.listarPorCotizacion(id);
    }

    @PostMapping("/{id}/versiones")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/cotizaciones', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public CotizacionVersionResponse crearVersion(@PathVariable Integer id,
                                                  @RequestParam(defaultValue = "true") boolean copiarUltimoDetalle) {
        return cotizacionVersionService.crearVersion(id, copiarUltimoDetalle);
    }
}
