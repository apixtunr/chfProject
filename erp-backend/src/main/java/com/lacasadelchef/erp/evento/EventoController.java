package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.cotizacion.dto.CotizacionResponse;
import com.lacasadelchef.erp.evento.dto.CambiarEstadoRequest;
import com.lacasadelchef.erp.evento.dto.EventoRequest;
import com.lacasadelchef.erp.evento.dto.EventoResponse;
import com.lacasadelchef.erp.evento.dto.EventoResumenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    @GetMapping
    public Page<EventoResponse> listar(@RequestParam(required = false) LocalDate fechaDesde,
                                       @RequestParam(required = false) LocalDate fechaHasta,
                                       @RequestParam(required = false) Integer idCliente,
                                       @RequestParam(required = false) Integer idTipoEvento,
                                       @RequestParam(required = false) Integer idEstado,
                                       @PageableDefault(size = 20, sort = "fechaEvento") Pageable pageable) {
        return eventoService.listar(fechaDesde, fechaHasta, idCliente, idTipoEvento, idEstado, pageable);
    }

    @GetMapping("/resumen")
    public EventoResumenResponse resumen(@RequestParam(required = false) LocalDate fechaDesde,
                                         @RequestParam(required = false) LocalDate fechaHasta,
                                         @RequestParam(required = false) Integer idCliente,
                                         @RequestParam(required = false) Integer idTipoEvento) {
        return eventoService.resumen(fechaDesde, fechaHasta, idCliente, idTipoEvento);
    }

    @GetMapping("/cotizaciones-disponibles")
    public List<CotizacionResponse> cotizacionesDisponibles() {
        return eventoService.listarCotizacionesDisponibles();
    }

    @GetMapping("/{id}")
    public EventoResponse obtener(@PathVariable Integer id) {
        return eventoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public EventoResponse crear(@Valid @RequestBody EventoRequest request) {
        return eventoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public EventoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody EventoRequest request) {
        return eventoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        eventoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public EventoResponse cambiarEstado(@PathVariable Integer id, @Valid @RequestBody CambiarEstadoRequest request) {
        return eventoService.cambiarEstado(id, request.idEstado());
    }

    @PutMapping("/{id}/planificar")
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public EventoResponse planificar(@PathVariable Integer id) {
        return eventoService.planificar(id);
    }
}
