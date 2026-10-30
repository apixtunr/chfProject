package com.lacasadelchef.erp.rentabilidad;

import com.lacasadelchef.erp.common.pdf.ReportePdf;
import com.lacasadelchef.erp.common.pdf.ReportePdf.Columna;
import com.lacasadelchef.erp.common.pdf.ReportePdf.Indicador;
import com.lacasadelchef.erp.common.reporte.ReporteTablaPdf;
import com.lacasadelchef.erp.rentabilidad.dto.RentabilidadEventoResponse;
import com.lacasadelchef.erp.rentabilidad.dto.RentabilidadResumenResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentabilidadPdfService {

    private final RentabilidadService rentabilidadService;

    @Transactional(readOnly = true)
    public byte[] generar(LocalDate fechaDesde, LocalDate fechaHasta, Integer idCliente, Integer idTipoEvento,
                          String filtros) {
        RentabilidadResumenResponse r = rentabilidadService.resumen(fechaDesde, fechaHasta, idCliente, idTipoEvento);
        List<RentabilidadEventoResponse> eventos = rentabilidadService
                .listarPorEvento(fechaDesde, fechaHasta, idCliente, idTipoEvento, Pageable.unpaged())
                .getContent();

        ReportePdf pdf = ReportePdf.cartaHorizontal()
                .titulo("Análisis de rentabilidad", ReporteTablaPdf.subtitulo(fechaDesde, fechaHasta, filtros))
                .indicadores(List.of(
                        new Indicador("Ingresos proyectados", ReportePdf.moneda(r.ingresosAcordados()),
                                r.cantidadEventos() + (r.cantidadEventos() == 1 ? " evento finalizado" : " eventos finalizados")),
                        new Indicador("Cobrado", ReportePdf.moneda(r.cobrado()),
                                "Por cobrar: " + ReportePdf.moneda(r.porCobrar())),
                        new Indicador("Costos", ReportePdf.moneda(r.totalCostos()), "Personal, inventario y extras"),
                        new Indicador("Ganancia proyectada", ReportePdf.moneda(r.gananciaAcordada()),
                                "Margen: " + porcentaje(r.margen())),
                        new Indicador("Ganancia cobrada", ReportePdf.moneda(r.gananciaCobrada()), "Rentabilidad real")));

        pdf.tabla("Desglose de costos",
                List.of(Columna.texto("Concepto", 3), Columna.monto("Monto", 2), Columna.monto("Porcentaje", 1.5f)),
                List.of(costo("Personal", r.costoPersonal(), r.totalCostos()),
                        costo("Inventario", r.costoInventario(), r.totalCostos()),
                        costo("Costos extra", r.costoExtra(), r.totalCostos())),
                Set.of(),
                List.of("Total", ReportePdf.moneda(r.totalCostos()), r.totalCostos().signum() == 0 ? "" : "100.0 %"),
                "No hay costos en este periodo");

        pdf.tabla("Ganancia por tipo de evento",
                List.of(Columna.texto("Tipo de evento", 3), Columna.monto("Eventos", 1.2f),
                        Columna.monto("Ingresos proyectados", 2.2f), Columna.monto("Ganancia proyectada", 2.2f),
                        Columna.monto("Margen", 1.4f)),
                r.porTipo().stream()
                        .map(t -> List.of(t.tipoEventoNombre(), String.valueOf(t.cantidadEventos()),
                                ReportePdf.moneda(t.ingresosAcordados()), ReportePdf.moneda(t.gananciaAcordada()),
                                porcentaje(t.margen())))
                        .toList(),
                Set.of(), null, "No hay eventos finalizados en este periodo");

        pdf.tabla("Detalle por evento",
                List.of(Columna.texto("Evento", 1), Columna.texto("Fecha", 1.3f), Columna.texto("Cliente", 3.2f),
                        Columna.texto("Tipo", 1.8f), Columna.monto("Proyectado", 1.7f), Columna.monto("Cobrado", 1.7f),
                        Columna.monto("Costos", 1.7f), Columna.monto("Ganancia", 1.7f), Columna.monto("Margen", 1.2f)),
                eventos.stream()
                        .map(e -> List.of("#" + e.idEvento(), ReportePdf.fecha(e.fechaEvento()), e.clienteNombre(),
                                e.tipoEventoNombre(), ReportePdf.moneda(e.ingresosAcordados()),
                                ReportePdf.moneda(e.cobrado()), ReportePdf.moneda(e.totalCostos()),
                                ReportePdf.moneda(e.gananciaAcordada()), porcentaje(e.margen())))
                        .toList(),
                Set.of(),
                eventos.isEmpty() ? null : List.of("Total", "", "", "", ReportePdf.moneda(r.ingresosAcordados()),
                        ReportePdf.moneda(r.cobrado()), ReportePdf.moneda(r.totalCostos()),
                        ReportePdf.moneda(r.gananciaAcordada()), porcentaje(r.margen())),
                "No hay eventos finalizados en este periodo");

        return pdf.cerrar();
    }

    private static List<String> costo(String concepto, BigDecimal monto, BigDecimal total) {
        String parte = total.signum() == 0 ? "" : porcentaje(monto.multiply(BigDecimal.valueOf(100))
                .divide(total, 1, RoundingMode.HALF_UP));
        return List.of(concepto, ReportePdf.moneda(monto), parte);
    }

    private static String porcentaje(BigDecimal valor) {
        return valor == null ? "" : valor.setScale(1, RoundingMode.HALF_UP) + " %";
    }
}
