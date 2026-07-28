package com.lacasadelchef.erp.administracion.usuario;

import com.lacasadelchef.erp.administracion.usuario.dto.CambiarPasswordRequest;
import com.lacasadelchef.erp.administracion.usuario.dto.UsuarioActualizarRequest;
import com.lacasadelchef.erp.administracion.usuario.dto.UsuarioRequest;
import com.lacasadelchef.erp.administracion.usuario.dto.UsuarioResponse;
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
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public Page<UsuarioResponse> listar(@RequestParam(required = false) String username,
                                        @PageableDefault(size = 20, sort = "username") Pageable pageable) {
        return usuarioService.listar(username, pageable);
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Integer id) {
        return usuarioService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/usuarios', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest request) {
        return usuarioService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/usuarios', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public UsuarioResponse actualizar(@PathVariable Integer id, @Valid @RequestBody UsuarioActualizarRequest request) {
        return usuarioService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/usuarios', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("@permisoService.tienePermiso('/api/usuarios', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public ResponseEntity<Void> cambiarPassword(@PathVariable Integer id, @Valid @RequestBody CambiarPasswordRequest request) {
        usuarioService.cambiarPassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/desbloquear")
    @PreAuthorize("@permisoService.tienePermiso('/api/usuarios', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public UsuarioResponse desbloquear(@PathVariable Integer id) {
        return usuarioService.desbloquear(id);
    }
}
