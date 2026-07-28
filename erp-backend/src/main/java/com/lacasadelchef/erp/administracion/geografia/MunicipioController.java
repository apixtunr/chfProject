package com.lacasadelchef.erp.administracion.geografia;

import com.lacasadelchef.erp.administracion.geografia.dto.MunicipioRequest;
import com.lacasadelchef.erp.administracion.geografia.dto.MunicipioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/municipios")
@RequiredArgsConstructor
public class MunicipioController {

    private final MunicipioService municipioService;

    @GetMapping
    public List<MunicipioResponse> listar(@RequestParam(required = false) Integer idDepartamento) {
        return municipioService.listar(idDepartamento);
    }

    @GetMapping("/{id}")
    public MunicipioResponse obtener(@PathVariable Integer id) {
        return municipioService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/municipios', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public MunicipioResponse crear(@Valid @RequestBody MunicipioRequest request) {
        return municipioService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/municipios', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public MunicipioResponse actualizar(@PathVariable Integer id, @Valid @RequestBody MunicipioRequest request) {
        return municipioService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/municipios', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        municipioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
