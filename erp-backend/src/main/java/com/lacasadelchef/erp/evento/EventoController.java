package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.pdf.ReportePdf;
import com.lacasadelchef.erp.common.reporte.ReporteTabla;
import com.lacasadelchef.erp.common.reporte.ReporteTablaPdf;
import com.lacasadelchef.erp.cotizacion.dto.CotizacionResponse;
import com.lacasadelchef.erp.evento.dto.AnticipoResponse;
import com.lacasadelchef.erp.evento.dto.CambiarEstadoRequest;
import com.lacasadelchef.erp.evento.dto.CancelarEventoRequest;
import com.lacasadelchef.erp.evento.dto.EventoRequest;
import com.lacasadelchef.erp.evento.dto.EventoResponse;
import com.lacasadelchef.erp.evento.dto.EventoResumenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
    private final HojaServicioPdfService hojaServicioPdfService;
    private final EventoReporteService eventoReporteService;

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

    /** Reporte de eventos por periodo; fechaSegun = REGISTRO usa el dia en que se registro el evento. */
    @GetMapping("/reportes/eventos")
    public ReporteTabla reporteEventos(
            @RequestParam(required = false) String fechaSegun,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Integer idEstado,
            @RequestParam(required = false) Integer idTipoEvento,
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) String agrupar) {
        return eventoReporteService.eventos(fechaSegun, fechaDesde, fechaHasta, idEstado, idTipoEvento, idCliente, agrupar);
    }

    @GetMapping("/reportes/eventos/pdf")
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).IMPRIMIR)")
    public ResponseEntity<byte[]> reporteEventosPdf(
            @RequestParam(required = false) String fechaSegun,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Integer idEstado,
            @RequestParam(required = false) Integer idTipoEvento,
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) String agrupar,
            @RequestParam(required = false) String filtros) {
        ReporteTabla r = eventoReporteService.eventos(fechaSegun, fechaDesde, fechaHasta, idEstado, idTipoEvento,
                idCliente, agrupar);
        return ReporteTablaPdf.respuesta(r, ReporteTablaPdf.subtitulo(fechaDesde, fechaHasta, filtros), "eventos");
    }

    @GetMapping("/cotizaciones-disponibles")
    public List<CotizacionResponse> cotizacionesDisponibles() {
        return eventoService.listarCotizacionesDisponibles();
    }

    @GetMapping("/{id}")
    public EventoResponse obtener(@PathVariable Integer id) {
        return eventoService.obtenerPorId(id);
    }

    /** Hoja de servicio en PDF: lo que el personal necesita el dia del evento, sin montos. */
    @GetMapping("/{id}/hoja-servicio")
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).IMPRIMIR)")
    public ResponseEntity<byte[]> hojaServicio(@PathVariable Integer id) {
        return ReportePdf.respuesta(hojaServicioPdfService.generar(id), "hoja-servicio-evento-" + id);
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

    /** sinAnticipo=true: el usuario confirma planificar aunque el cliente no haya pagado el 50%. */
    @PutMapping("/{id}/planificar")
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public EventoResponse planificar(@PathVariable Integer id,
                                     @RequestParam(defaultValue = "false") boolean sinAnticipo) {
        return eventoService.planificar(id, sinAnticipo);
    }

    @GetMapping("/{id}/anticipo")
    public AnticipoResponse anticipo(@PathVariable Integer id) {
        return eventoService.anticipo(id);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("@permisoService.tienePermiso('/api/eventos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public EventoResponse cancelar(@PathVariable Integer id, @Valid @RequestBody CancelarEventoRequest request) {
        return eventoService.cancelar(id, request);
    }
}
