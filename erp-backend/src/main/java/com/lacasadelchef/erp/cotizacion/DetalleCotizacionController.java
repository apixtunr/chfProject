package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.cotizacion.dto.DetalleCotizacionRequest;
import com.lacasadelchef.erp.cotizacion.dto.DetalleCotizacionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cotizaciones/versiones/{idVersion}/detalles")
@RequiredArgsConstructor
public class DetalleCotizacionController {

    private final DetalleCotizacionService detalleCotizacionService;

    @GetMapping
    public List<DetalleCotizacionResponse> listar(@PathVariable Integer idVersion) {
        return detalleCotizacionService.listar(idVersion);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/cotizaciones', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public DetalleCotizacionResponse agregar(@PathVariable Integer idVersion,
                                             @Valid @RequestBody DetalleCotizacionRequest request) {
        return detalleCotizacionService.agregar(idVersion, request);
    }

    @PutMapping("/{idDetalle}")
    @PreAuthorize("@permisoService.tienePermiso('/api/cotizaciones', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public DetalleCotizacionResponse actualizar(@PathVariable Integer idVersion, @PathVariable Integer idDetalle,
                                                @Valid @RequestBody DetalleCotizacionRequest request) {
        return detalleCotizacionService.actualizar(idVersion, idDetalle, request);
    }

    @DeleteMapping("/{idDetalle}")
    @PreAuthorize("@permisoService.tienePermiso('/api/cotizaciones', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer idVersion, @PathVariable Integer idDetalle) {
        detalleCotizacionService.eliminar(idVersion, idDetalle);
        return ResponseEntity.noContent().build();
    }
}
