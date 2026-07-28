package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.evento.dto.CostoEventoRequest;
import com.lacasadelchef.erp.evento.dto.CostoEventoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos/{idEvento}/costos")
@RequiredArgsConstructor
public class CostoEventoController {

    private final CostoEventoService costoEventoService;

    @GetMapping
    public List<CostoEventoResponse> listar(@PathVariable Integer idEvento) {
        return costoEventoService.listar(idEvento);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public CostoEventoResponse agregar(@PathVariable Integer idEvento, @Valid @RequestBody CostoEventoRequest request) {
        return costoEventoService.agregar(idEvento, request);
    }

    @PutMapping("/{idCostoEvento}")
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public CostoEventoResponse actualizar(@PathVariable Integer idEvento, @PathVariable Integer idCostoEvento,
                                          @Valid @RequestBody CostoEventoRequest request) {
        return costoEventoService.actualizar(idEvento, idCostoEvento, request);
    }

    @DeleteMapping("/{idCostoEvento}")
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer idEvento, @PathVariable Integer idCostoEvento) {
        costoEventoService.eliminar(idEvento, idCostoEvento);
        return ResponseEntity.noContent().build();
    }
}
