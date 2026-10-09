package com.lacasadelchef.erp.cliente;

import com.lacasadelchef.erp.common.reporte.ReporteTabla;
import com.lacasadelchef.erp.common.reporte.ReporteTablaBuilder;
import com.lacasadelchef.erp.common.reporte.ReporteTablaPdf;
import com.lacasadelchef.erp.repository.ClienteRepository;
import com.lacasadelchef.erp.repository.ClienteRepository.ClienteReporteFila;
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

/** Reporte de clientes registrados por periodo (por el dia en que se dieron de alta). */
@RestController
@RequestMapping("/api/clientes/reportes/clientes")
@RequiredArgsConstructor
public class ClienteReporteController {

    private final ClienteRepository clienteRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public ReporteTabla clientes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Integer idEstado,
            @RequestParam(required = false) String agrupar) {
        return ReporteTablaBuilder.de("Clientes registrados", clienteRepository.reporteClientes(fechaDesde, fechaHasta, idEstado))
                .texto("cliente", "Cliente", 2.6f, ClienteReporteFila::getNombre)
                .texto("nit", "NIT", 1.1f, ClienteReporteFila::getNit)
                .texto("telefono", "Teléfono", 1.1f, ClienteReporteFila::getTelefono)
                .texto("correo", "Correo", 2.2f, ClienteReporteFila::getCorreo)
                .texto("municipio", "Municipio", 1.8f, c -> c.getMunicipio() == null ? ""
                        : c.getMunicipio() + (c.getDepartamento() != null ? ", " + c.getDepartamento() : ""))
                .texto("estado", "Estado", 1f, ClienteReporteFila::getEstado)
                .fecha("registro", "Registrado", 1.1f, c -> c.getFechaRegistro().toLocalDate())
                .fechaDeAgrupacion(c -> c.getFechaRegistro().toLocalDate())
                .agrupacion("ESTADO", ClienteReporteFila::getEstado)
                .agrupacion("DEPARTAMENTO", ClienteReporteFila::getDepartamento)
                .construir(agrupar);
    }

    @GetMapping("/pdf")
    @Transactional(readOnly = true)
    @PreAuthorize("@permisoService.tienePermiso('/api/clientes', T(com.lacasadelchef.erp.security.TipoPermiso).IMPRIMIR)")
    public ResponseEntity<byte[]> clientesPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Integer idEstado,
            @RequestParam(required = false) String agrupar,
            @RequestParam(required = false) String filtros) {
        return ReporteTablaPdf.respuesta(clientes(fechaDesde, fechaHasta, idEstado, agrupar),
                ReporteTablaPdf.subtitulo(fechaDesde, fechaHasta, filtros), "clientes");
    }
}
