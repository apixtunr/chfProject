package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.common.pdf.ReportePdf;
import com.lacasadelchef.erp.common.reporte.ReporteTabla;
import com.lacasadelchef.erp.common.reporte.ReporteTablaPdf;
import com.lacasadelchef.erp.pago.CuentaPorCobrar.Situacion;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Reportes de Pagos. Consultarlos exige el modulo de Pagos (RecursosApi); el PDF, ademas,
 * el permiso de imprimir. El CSV lo arma la pantalla con la misma respuesta.
 */
@RestController
@RequestMapping("/api/pagos/reportes")
@RequiredArgsConstructor
public class ReportePagoController {

    private final ReportePagoService reportePagoService;

    @GetMapping("/recibos")
    public ReporteTabla recibos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Integer idMetodoPago,
            @RequestParam(required = false) Integer idEstado,
            @RequestParam(required = false) String agrupar) {
        return reportePagoService.recibos(fechaDesde, fechaHasta, idMetodoPago, idEstado, agrupar);
    }

    @GetMapping("/recibos/pdf")
    @PreAuthorize("@permisoService.tienePermiso('/api/pagos', T(com.lacasadelchef.erp.security.TipoPermiso).IMPRIMIR)")
    public ResponseEntity<byte[]> recibosPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Integer idMetodoPago,
            @RequestParam(required = false) Integer idEstado,
            @RequestParam(required = false) String agrupar,
            @RequestParam(required = false) String filtros) {
        ReporteTabla r = reportePagoService.recibos(fechaDesde, fechaHasta, idMetodoPago, idEstado, agrupar);
        return ReporteTablaPdf.respuesta(r, ReporteTablaPdf.subtitulo(fechaDesde, fechaHasta, filtros), "recibos-emitidos");
    }

    @GetMapping("/cuentas-por-cobrar")
    public ReporteTabla cuentasPorCobrar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) Situacion situacion,
            @RequestParam(required = false) String agrupar) {
        return reportePagoService.cuentasPorCobrar(fechaDesde, fechaHasta, idCliente, situacion, agrupar);
    }

    @GetMapping("/cuentas-por-cobrar/pdf")
    @PreAuthorize("@permisoService.tienePermiso('/api/pagos', T(com.lacasadelchef.erp.security.TipoPermiso).IMPRIMIR)")
    public ResponseEntity<byte[]> cuentasPorCobrarPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) Situacion situacion,
            @RequestParam(required = false) String agrupar,
            @RequestParam(required = false) String filtros) {
        ReporteTabla r = reportePagoService.cuentasPorCobrar(fechaDesde, fechaHasta, idCliente, situacion, agrupar);
        // Es una foto del dia: lo que se debe hoy. Las fechas, si se dan, son las de los eventos.
        String sub = "Al " + ReportePdf.fecha(LocalDate.now());
        if (fechaDesde != null || fechaHasta != null) {
            sub += "  ·  Eventos: " + ReportePdf.periodo(fechaDesde, fechaHasta).toLowerCase();
        }
        if (filtros != null && !filtros.isBlank()) {
            sub += "  ·  " + filtros;
        }
        return ReporteTablaPdf.respuesta(r, sub, "cuentas-por-cobrar");
    }
}
