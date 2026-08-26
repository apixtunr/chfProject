package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.evento.dto.DetalleEventoRequest;
import com.lacasadelchef.erp.evento.dto.DetalleEventoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos/{idEvento}/detalles")
@RequiredArgsConstructor
public class DetalleEventoController {

    private final DetalleEventoService detalleEventoService;

    @GetMapping
    public List<DetalleEventoResponse> listar(@PathVariable Integer idEvento) {
        return detalleEventoService.listar(idEvento);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public DetalleEventoResponse agregar(@PathVariable Integer idEvento,
                                         @Valid @RequestBody DetalleEventoRequest request) {
        return detalleEventoService.agregar(idEvento, request);
    }

    @PutMapping("/{idDetalle}")
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public DetalleEventoResponse actualizar(@PathVariable Integer idEvento, @PathVariable Integer idDetalle,
                                            @Valid @RequestBody DetalleEventoRequest request) {
        return detalleEventoService.actualizar(idEvento, idDetalle, request);
    }

    @DeleteMapping("/{idDetalle}")
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer idEvento, @PathVariable Integer idDetalle) {
        detalleEventoService.eliminar(idEvento, idDetalle);
        return ResponseEntity.noContent().build();
    }
}
