package com.lacasadelchef.erp.administracion.flota;

import com.lacasadelchef.erp.administracion.flota.dto.LineaVehiculoRequest;
import com.lacasadelchef.erp.administracion.flota.dto.LineaVehiculoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lineas-vehiculo")
@RequiredArgsConstructor
public class LineaVehiculoController {

    private final LineaVehiculoService lineaVehiculoService;

    @GetMapping
    public List<LineaVehiculoResponse> listar(@RequestParam(required = false) Integer idMarcaVehiculo) {
        return lineaVehiculoService.listar(idMarcaVehiculo);
    }

    @GetMapping("/{id}")
    public LineaVehiculoResponse obtener(@PathVariable Integer id) {
        return lineaVehiculoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/lineas-vehiculo', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public LineaVehiculoResponse crear(@Valid @RequestBody LineaVehiculoRequest request) {
        return lineaVehiculoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/lineas-vehiculo', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public LineaVehiculoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody LineaVehiculoRequest request) {
        return lineaVehiculoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/lineas-vehiculo', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        lineaVehiculoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
