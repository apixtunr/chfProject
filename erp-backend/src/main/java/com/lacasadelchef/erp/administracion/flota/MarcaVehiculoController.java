package com.lacasadelchef.erp.administracion.flota;

import com.lacasadelchef.erp.administracion.flota.dto.MarcaVehiculoRequest;
import com.lacasadelchef.erp.administracion.flota.dto.MarcaVehiculoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marcas-vehiculo")
@RequiredArgsConstructor
public class MarcaVehiculoController {

    private final MarcaVehiculoService marcaVehiculoService;

    @GetMapping
    public List<MarcaVehiculoResponse> listar() {
        return marcaVehiculoService.listar();
    }

    @GetMapping("/{id}")
    public MarcaVehiculoResponse obtener(@PathVariable Integer id) {
        return marcaVehiculoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/marcas-vehiculo', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public MarcaVehiculoResponse crear(@Valid @RequestBody MarcaVehiculoRequest request) {
        return marcaVehiculoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/marcas-vehiculo', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public MarcaVehiculoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody MarcaVehiculoRequest request) {
        return marcaVehiculoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/marcas-vehiculo', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        marcaVehiculoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
