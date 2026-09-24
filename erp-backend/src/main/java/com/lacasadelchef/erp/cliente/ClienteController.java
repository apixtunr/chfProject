package com.lacasadelchef.erp.cliente;

import com.lacasadelchef.erp.cliente.dto.CambiarEstadoClienteRequest;
import com.lacasadelchef.erp.cliente.dto.ClienteRequest;
import com.lacasadelchef.erp.cliente.dto.ClienteResponse;
import com.lacasadelchef.erp.cliente.dto.PosibleDuplicadoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Clientes. No hay DELETE: un cliente se inactiva con PUT /{id}/estado, que usa el
 * permiso de BAJA (quien podia eliminar ahora puede inactivar y reactivar).
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    /**
     * buscar: texto libre sobre nombre, NIT, telefono y correo ("nombre" se acepta por
     * compatibilidad). estado: ACTIVO, INACTIVO o TODOS (por defecto).
     */
    @GetMapping
    public Page<ClienteResponse> listar(@RequestParam(required = false) String buscar,
                                        @RequestParam(required = false) String nombre,
                                        @RequestParam(required = false) String estado,
                                        @PageableDefault(size = 20, sort = "idCliente") Pageable pageable) {
        return clienteService.listar(buscar != null ? buscar : nombre, estado, pageable);
    }

    @GetMapping("/posibles-duplicados")
    public List<PosibleDuplicadoResponse> posiblesDuplicados(@RequestParam(required = false) String nombre,
                                                             @RequestParam(required = false) String nit,
                                                             @RequestParam(required = false) String telefono,
                                                             @RequestParam(required = false) String correo,
                                                             @RequestParam(required = false) Integer excluir) {
        return clienteService.posiblesDuplicados(nombre, nit, telefono, correo, excluir);
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

    @PutMapping("/{id}/estado")
    @PreAuthorize("@permisoService.tienePermiso('/api/clientes', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ClienteResponse cambiarEstado(@PathVariable Integer id,
                                         @Valid @RequestBody CambiarEstadoClienteRequest request) {
        return clienteService.cambiarEstado(id, request.activo());
    }
}
