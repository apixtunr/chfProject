package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.common.reporte.ReporteTabla;
import com.lacasadelchef.erp.common.reporte.ReporteTablaBuilder;
import com.lacasadelchef.erp.common.reporte.ReporteTablaPdf;
import com.lacasadelchef.erp.entity.CotizacionVersion;
import com.lacasadelchef.erp.repository.CotizacionVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Reporte de cotizaciones por periodo: cada cotizacion hecha en el periodo con su version
 * vigente, su estado y su monto. Agrupado por estado deja ver cuantas se aceptaron,
 * rechazaron o vencieron.
 */
@RestController
@RequestMapping("/api/cotizaciones/reportes")
@RequiredArgsConstructor
public class CotizacionReporteController {

    private final CotizacionVersionRepository cotizacionVersionRepository;

    @GetMapping("/cotizaciones")
    @Transactional(readOnly = true)
    public ReporteTabla cotizaciones(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Integer idEstado,
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) String agrupar) {
        return ReporteTablaBuilder.de("Cotizaciones por periodo",
                        cotizacionVersionRepository.reporteCotizaciones(fechaDesde, fechaHasta, idEstado, idCliente))
                .texto("cotizacion", "Cotización", 1f,
                        v -> "#%d v%d".formatted(v.getCotizacion().getIdCotizacion(), v.getNumeroVersion()))
                .fecha("fecha", "Fecha", 1f, v -> v.getCotizacion().getFechaCotizacion())
                .texto("cliente", "Cliente", 2.4f, v -> v.getCotizacion().getCliente().getNombre())
                .texto("tipo", "Tipo", 1.4f, v -> v.getCotizacion().getTipoEvento().getNombreTipo())
                .fecha("fechaEvento", "Fecha del evento", 1.2f, v -> v.getCotizacion().getFechaEvento())
                .numero("personas", "Personas", 0.9f, true, v -> v.getCotizacion().getCantidadPersonas())
                .texto("estado", "Estado", 1.2f, v -> v.getEstado().getNombre())
                .monto("monto", "Monto", 1.2f, true, CotizacionVersion::getMontoTotal)
                .fechaDeAgrupacion(v -> v.getCotizacion().getFechaCotizacion())
                .agrupacion("ESTADO", v -> v.getEstado().getNombre())
                .agrupacion("CLIENTE", v -> v.getCotizacion().getCliente().getNombre())
                .construir(agrupar);
    }

    @GetMapping("/cotizaciones/pdf")
    @Transactional(readOnly = true)
    @PreAuthorize("@permisoService.tienePermiso('/api/cotizaciones', T(com.lacasadelchef.erp.security.TipoPermiso).IMPRIMIR)")
    public ResponseEntity<byte[]> cotizacionesPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Integer idEstado,
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) String agrupar,
            @RequestParam(required = false) String filtros) {
        return ReporteTablaPdf.respuesta(cotizaciones(fechaDesde, fechaHasta, idEstado, idCliente, agrupar),
                ReporteTablaPdf.subtitulo(fechaDesde, fechaHasta, filtros), "cotizaciones");
    }
}
