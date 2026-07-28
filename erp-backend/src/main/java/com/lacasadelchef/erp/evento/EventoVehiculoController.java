package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.evento.dto.EventoVehiculoRequest;
import com.lacasadelchef.erp.evento.dto.EventoVehiculoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos/{idEvento}/vehiculos")
@RequiredArgsConstructor
public class EventoVehiculoController {

    private final EventoVehiculoService eventoVehiculoService;

    @GetMapping
    public List<EventoVehiculoResponse> listar(@PathVariable Integer idEvento) {
        return eventoVehiculoService.listar(idEvento);
    }

    @PostMapping("/{idVehiculo}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public EventoVehiculoResponse asignar(@PathVariable Integer idEvento, @PathVariable Integer idVehiculo,
                                          @Valid @RequestBody EventoVehiculoRequest request) {
        return eventoVehiculoService.asignar(idEvento, idVehiculo, request);
    }

    @PutMapping("/{idVehiculo}")
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public EventoVehiculoResponse actualizar(@PathVariable Integer idEvento, @PathVariable Integer idVehiculo,
                                             @Valid @RequestBody EventoVehiculoRequest request) {
        return eventoVehiculoService.actualizar(idEvento, idVehiculo, request);
    }

    @DeleteMapping("/{idVehiculo}")
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> quitar(@PathVariable Integer idEvento, @PathVariable Integer idVehiculo) {
        eventoVehiculoService.quitar(idEvento, idVehiculo);
        return ResponseEntity.noContent().build();
    }
}
