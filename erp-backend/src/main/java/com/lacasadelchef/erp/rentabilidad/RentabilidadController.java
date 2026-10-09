package com.lacasadelchef.erp.rentabilidad;

import com.lacasadelchef.erp.common.reporte.ReporteTabla;
import com.lacasadelchef.erp.common.reporte.ReporteTablaPdf;
import com.lacasadelchef.erp.rentabilidad.dto.RentabilidadEventoResponse;
import com.lacasadelchef.erp.rentabilidad.dto.RentabilidadResumenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Reportes de solo lectura: ingresos, costos y ganancia por evento (vistas SQL ya calculadas). */
@RestController
@RequestMapping("/api/rentabilidad")
@RequiredArgsConstructor
public class RentabilidadController {

    private final RentabilidadService rentabilidadService;
    private final PagoPersonalService pagoPersonalService;

    @GetMapping("/eventos")
    public Page<RentabilidadEventoResponse> listarPorEvento(@RequestParam(required = false) LocalDate fechaDesde,
                                                             @RequestParam(required = false) LocalDate fechaHasta,
                                                             @RequestParam(required = false) Integer idCliente,
                                                             @RequestParam(required = false) Integer idTipoEvento,
                                                             @PageableDefault(size = 20, sort = "fechaEvento") Pageable pageable) {
        return rentabilidadService.listarPorEvento(fechaDesde, fechaHasta, idCliente, idTipoEvento, pageable);
    }

    @GetMapping("/eventos/{idEvento}")
    public RentabilidadEventoResponse obtenerPorEvento(@PathVariable Integer idEvento) {
        return rentabilidadService.obtenerPorEvento(idEvento);
    }

    @GetMapping("/resumen")
    public RentabilidadResumenResponse resumen(@RequestParam(required = false) LocalDate fechaDesde,
                                               @RequestParam(required = false) LocalDate fechaHasta,
                                               @RequestParam(required = false) Integer idCliente,
                                               @RequestParam(required = false) Integer idTipoEvento) {
        return rentabilidadService.resumen(fechaDesde, fechaHasta, idCliente, idTipoEvento);
    }

    /** Lo que se le pago a cada persona en los eventos finalizados del periodo. */
    @GetMapping("/reportes/pago-personal")
    public ReporteTabla pagoPersonal(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String agrupar) {
        return pagoPersonalService.generar(fechaDesde, fechaHasta, agrupar);
    }

    @GetMapping("/reportes/pago-personal/pdf")
    @PreAuthorize("@permisoService.tienePermiso('/api/rentabilidad', T(com.lacasadelchef.erp.security.TipoPermiso).IMPRIMIR)")
    public ResponseEntity<byte[]> pagoPersonalPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String agrupar,
            @RequestParam(required = false) String filtros) {
        return ReporteTablaPdf.respuesta(pagoPersonalService.generar(fechaDesde, fechaHasta, agrupar),
                ReporteTablaPdf.subtitulo(fechaDesde, fechaHasta, filtros), "pago-al-personal");
    }
}
