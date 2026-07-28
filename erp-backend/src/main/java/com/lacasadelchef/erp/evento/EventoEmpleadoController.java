package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.evento.dto.EventoEmpleadoRequest;
import com.lacasadelchef.erp.evento.dto.EventoEmpleadoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos/{idEvento}/empleados")
@RequiredArgsConstructor
public class EventoEmpleadoController {

    private final EventoEmpleadoService eventoEmpleadoService;

    @GetMapping
    public List<EventoEmpleadoResponse> listar(@PathVariable Integer idEvento) {
        return eventoEmpleadoService.listar(idEvento);
    }

    @PostMapping("/{idEmpleado}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public EventoEmpleadoResponse asignar(@PathVariable Integer idEvento, @PathVariable Integer idEmpleado,
                                          @Valid @RequestBody EventoEmpleadoRequest request) {
        return eventoEmpleadoService.asignar(idEvento, idEmpleado, request);
    }

    @PutMapping("/{idEmpleado}")
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public EventoEmpleadoResponse actualizar(@PathVariable Integer idEvento, @PathVariable Integer idEmpleado,
                                             @Valid @RequestBody EventoEmpleadoRequest request) {
        return eventoEmpleadoService.actualizar(idEvento, idEmpleado, request);
    }

    @DeleteMapping("/{idEmpleado}")
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> quitar(@PathVariable Integer idEvento, @PathVariable Integer idEmpleado) {
        eventoEmpleadoService.quitar(idEvento, idEmpleado);
        return ResponseEntity.noContent().build();
    }
}
