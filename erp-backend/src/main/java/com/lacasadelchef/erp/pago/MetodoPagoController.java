package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.pago.dto.MetodoPagoRequest;
import com.lacasadelchef.erp.pago.dto.MetodoPagoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metodos-pago")
@RequiredArgsConstructor
public class MetodoPagoController {

    private final MetodoPagoService metodoPagoService;

    @GetMapping
    public List<MetodoPagoResponse> listar() {
        return metodoPagoService.listar();
    }

    @GetMapping("/{id}")
    public MetodoPagoResponse obtener(@PathVariable Integer id) {
        return metodoPagoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/metodos-pago', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public MetodoPagoResponse crear(@Valid @RequestBody MetodoPagoRequest request) {
        return metodoPagoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/metodos-pago', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public MetodoPagoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody MetodoPagoRequest request) {
        return metodoPagoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/metodos-pago', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        metodoPagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
