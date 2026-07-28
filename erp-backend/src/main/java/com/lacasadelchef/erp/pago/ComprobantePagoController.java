package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.pago.dto.ComprobantePagoRequest;
import com.lacasadelchef.erp.pago.dto.ComprobantePagoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos/{idPago}/comprobantes")
@RequiredArgsConstructor
public class ComprobantePagoController {

    private final ComprobantePagoService comprobantePagoService;

    @GetMapping
    public List<ComprobantePagoResponse> listar(@PathVariable Integer idPago) {
        return comprobantePagoService.listar(idPago);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/pagos', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public ComprobantePagoResponse agregar(@PathVariable Integer idPago,
                                           @Valid @RequestBody ComprobantePagoRequest request) {
        return comprobantePagoService.agregar(idPago, request);
    }

    @PutMapping("/{idComprobante}")
    @PreAuthorize("@permisoService.tienePermiso('/api/pagos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public ComprobantePagoResponse actualizar(@PathVariable Integer idPago, @PathVariable Integer idComprobante,
                                              @Valid @RequestBody ComprobantePagoRequest request) {
        return comprobantePagoService.actualizar(idPago, idComprobante, request);
    }

    @DeleteMapping("/{idComprobante}")
    @PreAuthorize("@permisoService.tienePermiso('/api/pagos', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer idPago, @PathVariable Integer idComprobante) {
        comprobantePagoService.eliminar(idPago, idComprobante);
        return ResponseEntity.noContent().build();
    }
}
