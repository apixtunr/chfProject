package com.lacasadelchef.erp.administracion.rrhh;

import com.lacasadelchef.erp.administracion.rrhh.dto.DocumentoEmpleadoRequest;
import com.lacasadelchef.erp.administracion.rrhh.dto.DocumentoEmpleadoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleados/{idEmpleado}/documentos")
@RequiredArgsConstructor
public class DocumentoEmpleadoController {

    private final DocumentoEmpleadoService documentoEmpleadoService;

    @GetMapping
    public List<DocumentoEmpleadoResponse> listar(@PathVariable Integer idEmpleado) {
        return documentoEmpleadoService.listar(idEmpleado);
    }

    @PostMapping("/{idTipoDocumento}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/empleados', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public DocumentoEmpleadoResponse agregar(@PathVariable Integer idEmpleado, @PathVariable Integer idTipoDocumento,
                                             @Valid @RequestBody DocumentoEmpleadoRequest request) {
        return documentoEmpleadoService.agregar(idEmpleado, idTipoDocumento, request);
    }

    @PutMapping("/{idTipoDocumento}")
    @PreAuthorize("@permisoService.tienePermiso('/api/empleados', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public DocumentoEmpleadoResponse actualizar(@PathVariable Integer idEmpleado, @PathVariable Integer idTipoDocumento,
                                                @Valid @RequestBody DocumentoEmpleadoRequest request) {
        return documentoEmpleadoService.actualizar(idEmpleado, idTipoDocumento, request);
    }

    @DeleteMapping("/{idTipoDocumento}")
    @PreAuthorize("@permisoService.tienePermiso('/api/empleados', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer idEmpleado, @PathVariable Integer idTipoDocumento) {
        documentoEmpleadoService.eliminar(idEmpleado, idTipoDocumento);
        return ResponseEntity.noContent().build();
    }
}
