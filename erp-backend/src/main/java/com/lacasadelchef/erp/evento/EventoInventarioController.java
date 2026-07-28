package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.evento.dto.EventoInventarioRequest;
import com.lacasadelchef.erp.evento.dto.EventoInventarioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos/{idEvento}/inventario")
@RequiredArgsConstructor
public class EventoInventarioController {

    private final EventoInventarioService eventoInventarioService;

    @GetMapping
    public List<EventoInventarioResponse> listar(@PathVariable Integer idEvento) {
        return eventoInventarioService.listar(idEvento);
    }

    @PostMapping("/{idProducto}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public EventoInventarioResponse agregar(@PathVariable Integer idEvento, @PathVariable Integer idProducto,
                                            @Valid @RequestBody EventoInventarioRequest request) {
        return eventoInventarioService.agregar(idEvento, idProducto, request);
    }

    @PutMapping("/{idProducto}")
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public EventoInventarioResponse actualizar(@PathVariable Integer idEvento, @PathVariable Integer idProducto,
                                               @Valid @RequestBody EventoInventarioRequest request) {
        return eventoInventarioService.actualizar(idEvento, idProducto, request);
    }

    @DeleteMapping("/{idProducto}")
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> quitar(@PathVariable Integer idEvento, @PathVariable Integer idProducto) {
        eventoInventarioService.quitar(idEvento, idProducto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{idProducto}/consumir")
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public EventoInventarioResponse confirmarConsumo(@PathVariable Integer idEvento, @PathVariable Integer idProducto) {
        return eventoInventarioService.confirmarConsumo(idEvento, idProducto);
    }
}
