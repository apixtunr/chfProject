package com.lacasadelchef.erp.inventario;

import com.lacasadelchef.erp.common.reporte.ReporteTabla;
import com.lacasadelchef.erp.common.reporte.ReporteTablaBuilder;
import com.lacasadelchef.erp.common.reporte.ReporteTablaPdf;
import com.lacasadelchef.erp.entity.MovimientoInventario;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.repository.MovimientoInventarioRepository;
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
 * Reporte de movimientos de inventario por periodo (entradas, salidas y ajustes). Las
 * cantidades no se suman en el total general porque cada producto tiene su unidad; al
 * agrupar por producto, cada grupo si muestra su cantidad.
 */
@RestController
@RequestMapping("/api/movimientos-inventario/reportes/movimientos")
@RequiredArgsConstructor
public class MovimientoReporteController {

    private final MovimientoInventarioRepository movimientoInventarioRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public ReporteTabla movimientos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String tipoMovimiento,
            @RequestParam(required = false) Integer idProducto,
            @RequestParam(required = false) String agrupar) {
        var movimientos = movimientoInventarioRepository.reporteMovimientos(
                fechaDesde == null ? null : fechaDesde.atStartOfDay(),
                fechaHasta == null ? null : fechaHasta.plusDays(1).atStartOfDay(),
                tipoMovimiento, idProducto);
        return ReporteTablaBuilder.de("Movimientos de inventario", movimientos)
                .fechaHora("fecha", "Fecha", 1.4f, MovimientoInventario::getFechaMovimiento)
                .texto("producto", "Producto", 2.4f, m -> m.getProducto().getNombreProducto())
                .texto("categoria", "Categoría", 1.4f, m -> m.getProducto().getCategoria() != null
                        ? m.getProducto().getCategoria().getNombreCategoria() : "")
                .texto("tipo", "Tipo", 1f, MovimientoInventario::getTipoMovimiento)
                .numero("cantidad", "Cantidad", 0.9f, false, MovimientoInventario::getCantidad)
                .texto("unidad", "Unidad", 0.9f, m -> m.getProducto().getUnidadMedida())
                .texto("evento", "Evento", 0.9f, m -> m.getEvento() != null ? "#" + m.getEvento().getIdEvento() : "")
                .texto("descripcion", "Descripción", 2.2f, MovimientoInventario::getDescripcion)
                .texto("usuario", "Registrado por", 1.6f, m -> nombreDe(m.getUsuario()))
                .fechaDeAgrupacion(m -> m.getFechaMovimiento().toLocalDate())
                .agrupacion("PRODUCTO", m -> m.getProducto().getNombreProducto())
                .agrupacion("TIPO", MovimientoInventario::getTipoMovimiento)
                .construir(agrupar);
    }

    @GetMapping("/pdf")
    @Transactional(readOnly = true)
    @PreAuthorize("@permisoService.tienePermiso('/api/movimientos-inventario', T(com.lacasadelchef.erp.security.TipoPermiso).IMPRIMIR)")
    public ResponseEntity<byte[]> movimientosPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String tipoMovimiento,
            @RequestParam(required = false) Integer idProducto,
            @RequestParam(required = false) String agrupar,
            @RequestParam(required = false) String filtros) {
        return ReporteTablaPdf.respuesta(movimientos(fechaDesde, fechaHasta, tipoMovimiento, idProducto, agrupar),
                ReporteTablaPdf.subtitulo(fechaDesde, fechaHasta, filtros), "movimientos-inventario");
    }

    private static String nombreDe(Usuario usuario) {
        if (usuario == null) {
            return "";
        }
        return usuario.getEmpleado() != null ? usuario.getEmpleado().getNombreCompleto() : usuario.getUsername();
    }
}
