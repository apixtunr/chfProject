package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.cotizacion.dto.ServicioCotizacionRequest;
import com.lacasadelchef.erp.cotizacion.dto.ServicioCotizacionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cotizaciones/versiones/{idVersion}/servicios")
@RequiredArgsConstructor
public class ServicioCotizacionController {

    private final ServicioCotizacionService servicioCotizacionService;

    @GetMapping
    public List<ServicioCotizacionResponse> listar(@PathVariable Integer idVersion) {
        return servicioCotizacionService.listar(idVersion);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/cotizaciones', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public ServicioCotizacionResponse agregar(@PathVariable Integer idVersion,
                                              @Valid @RequestBody ServicioCotizacionRequest request) {
        return servicioCotizacionService.agregar(idVersion, request);
    }

    @PutMapping("/{idServicio}")
    @PreAuthorize("@permisoService.tienePermiso('/api/cotizaciones', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public ServicioCotizacionResponse actualizar(@PathVariable Integer idVersion, @PathVariable Integer idServicio,
                                                 @Valid @RequestBody ServicioCotizacionRequest request) {
        return servicioCotizacionService.actualizar(idVersion, idServicio, request);
    }

    @DeleteMapping("/{idServicio}")
    @PreAuthorize("@permisoService.tienePermiso('/api/cotizaciones', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer idVersion, @PathVariable Integer idServicio) {
        servicioCotizacionService.eliminar(idVersion, idServicio);
        return ResponseEntity.noContent().build();
    }
}
