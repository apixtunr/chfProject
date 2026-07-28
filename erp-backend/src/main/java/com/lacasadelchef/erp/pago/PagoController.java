package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.pago.dto.CambiarEstadoRequest;
import com.lacasadelchef.erp.pago.dto.PagoRequest;
import com.lacasadelchef.erp.pago.dto.PagoResponse;
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
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @GetMapping
    public Page<PagoResponse> listar(@RequestParam(required = false) Integer idEvento,
                                     @PageableDefault(size = 20, sort = "fechaPago") Pageable pageable) {
        return pagoService.listar(idEvento, pageable);
    }

    @GetMapping("/{id}")
    public PagoResponse obtener(@PathVariable Integer id) {
        return pagoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/pagos', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public PagoResponse crear(@Valid @RequestBody PagoRequest request) {
        return pagoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/pagos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public PagoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody PagoRequest request) {
        return pagoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/pagos', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        pagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("@permisoService.tienePermiso('/api/pagos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public PagoResponse cambiarEstado(@PathVariable Integer id, @Valid @RequestBody CambiarEstadoRequest request) {
        return pagoService.cambiarEstado(id, request.idEstado());
    }
}
