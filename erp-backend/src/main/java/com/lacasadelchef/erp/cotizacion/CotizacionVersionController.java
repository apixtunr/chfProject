package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.cotizacion.dto.CambiarEstadoRequest;
import com.lacasadelchef.erp.cotizacion.dto.CotizacionVersionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cotizaciones/versiones")
@RequiredArgsConstructor
public class CotizacionVersionController {

    private final CotizacionVersionService cotizacionVersionService;
    private final CotizacionPdfService cotizacionPdfService;

    @GetMapping("/{id}")
    public CotizacionVersionResponse obtener(@PathVariable Integer id) {
        return cotizacionVersionService.obtenerPorId(id);
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("@permisoService.tienePermiso('/api/cotizaciones', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public CotizacionVersionResponse cambiarEstado(@PathVariable Integer id,
                                                    @Valid @RequestBody CambiarEstadoRequest request) {
        return cotizacionVersionService.cambiarEstado(id, request.idEstado());
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("@permisoService.tienePermiso('/api/cotizaciones', T(com.lacasadelchef.erp.security.TipoPermiso).IMPRIMIR)")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Integer id) {
        byte[] pdf = cotizacionPdfService.generar(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cotizacion-%d.pdf\"".formatted(id))
                .body(pdf);
    }
}
