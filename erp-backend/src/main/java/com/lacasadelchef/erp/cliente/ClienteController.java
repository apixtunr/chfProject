package com.lacasadelchef.erp.cliente;

import com.lacasadelchef.erp.cliente.dto.ClienteRequest;
import com.lacasadelchef.erp.cliente.dto.ClienteResponse;
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
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public Page<ClienteResponse> listar(@RequestParam(required = false) String nombre,
                                        @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        return clienteService.listar(nombre, pageable);
    }

    @GetMapping("/{id}")
    public ClienteResponse obtener(@PathVariable Integer id) {
        return clienteService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/clientes', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public ClienteResponse crear(@Valid @RequestBody ClienteRequest request) {
        return clienteService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/clientes', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public ClienteResponse actualizar(@PathVariable Integer id,
                                      @Valid @RequestBody ClienteRequest request) {
        return clienteService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/clientes', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
