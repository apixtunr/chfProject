package com.lacasadelchef.erp.bebida;

import com.lacasadelchef.erp.bebida.dto.BebidaRequest;
import com.lacasadelchef.erp.bebida.dto.BebidaResponse;
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
@RequestMapping("/api/bebidas")
@RequiredArgsConstructor
public class BebidaController {

    private final BebidaService bebidaService;

    @GetMapping
    public Page<BebidaResponse> listar(@RequestParam(required = false) String nombre,
                                       @PageableDefault(size = 20, sort = "nombreBebida") Pageable pageable) {
        return bebidaService.listar(nombre, pageable);
    }

    @GetMapping("/activas")
    public List<BebidaResponse> listarActivas() {
        return bebidaService.listarActivas();
    }

    @GetMapping("/{id}")
    public BebidaResponse obtener(@PathVariable Integer id) {
        return bebidaService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/bebidas', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public BebidaResponse crear(@Valid @RequestBody BebidaRequest request) {
        return bebidaService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/bebidas', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public BebidaResponse actualizar(@PathVariable Integer id, @Valid @RequestBody BebidaRequest request) {
        return bebidaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/bebidas', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        bebidaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
