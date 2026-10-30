package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.pdf.ReportePdf;
import com.lacasadelchef.erp.common.pdf.ReportePdf.Columna;
import com.lacasadelchef.erp.common.pdf.ReportePdf.Indicador;
import com.lacasadelchef.erp.common.reporte.ReporteTablaPdf;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.evento.dto.ConteoResponse;
import com.lacasadelchef.erp.evento.dto.EventoResponse;
import com.lacasadelchef.erp.evento.dto.EventoResumenResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventoEstadisticasPdfService {

    private static final int MAXIMO_EVENTOS = 5000;
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final EventoService eventoService;

    @Transactional(readOnly = true)
    public byte[] generar(LocalDate fechaDesde, LocalDate fechaHasta, Integer idCliente, Integer idTipoEvento,
                          Integer idEstado, String filtros) {
        EventoResumenResponse r = eventoService.resumen(fechaDesde, fechaHasta, idCliente, idTipoEvento);
        Sort porFecha = Sort.by(Evento::getFechaEvento);
        List<EventoResponse> eventos = eventoService.listar(fechaDesde, fechaHasta, idCliente, idTipoEvento, idEstado,
                PageRequest.of(0, MAXIMO_EVENTOS, porFecha)).getContent();

        ReportePdf pdf = ReportePdf.cartaHorizontal()
                .titulo("Estadísticas de eventos", ReporteTablaPdf.subtitulo(fechaDesde, fechaHasta, filtros))
                .indicadores(List.of(
                        new Indicador("Eventos", String.valueOf(r.totalEventos()), "Registrados en el periodo"),
                        new Indicador("Finalizados", String.valueOf(cantidad(r, "FINALIZADO")), "Realizados"),
                        new Indicador("Por realizar", String.valueOf(cantidad(r, "CREADO", "PLANIFICADO", "EN CURSO")),
                                "Creados, planificados o en curso"),
                        new Indicador("Cancelados", String.valueOf(cantidad(r, "CANCELADO")), "No se realizaron"),
                        new Indicador("Personas atendidas", String.valueOf(r.personasAtendidas()),
                                "En eventos finalizados")));

        pdf.tabla("Eventos por estado", columnasConteo("Estado"), filasConteo(r.porEstado(), r.totalEventos()),
                Set.of(), List.of("Total", String.valueOf(r.totalEventos()), r.totalEventos() == 0 ? "" : "100.0 %"),
                "No hay eventos en este periodo");
        pdf.tabla("Eventos por tipo", columnasConteo("Tipo de evento"), filasConteo(r.porTipo(), r.totalEventos()),
                Set.of(), null, "No hay eventos en este periodo");

        pdf.tabla("Detalle de eventos",
                List.of(Columna.texto("Evento", 1), Columna.texto("Fecha", 1.3f), Columna.texto("Horario", 1.5f),
                        Columna.texto("Cliente", 3.4f), Columna.texto("Tipo", 1.9f), Columna.monto("Personas", 1.2f),
                        Columna.texto("Estado", 1.6f)),
                eventos.stream()
                        .map(e -> List.of("#" + e.idEvento(), ReportePdf.fecha(e.fechaEvento()), horario(e),
                                texto(e.clienteNombre()), texto(e.tipoEventoNombre()),
                                e.cantidadPersonas() == null ? "" : String.valueOf(e.cantidadPersonas()),
                                texto(e.estadoNombre())))
                        .toList(),
                Set.of(), null, "No hay eventos que cumplan el filtro");

        return pdf.cerrar();
    }

    private static long cantidad(EventoResumenResponse r, String... estados) {
        Set<String> buscados = Set.of(estados);
        return r.porEstado().stream()
                .filter(c -> buscados.contains(c.etiqueta()))
                .mapToLong(ConteoResponse::cantidad)
                .sum();
    }

    private static List<Columna> columnasConteo(String titulo) {
        return List.of(Columna.texto(titulo, 3), Columna.monto("Eventos", 1.2f), Columna.monto("Porcentaje", 1.4f));
    }

    private static List<List<String>> filasConteo(List<ConteoResponse> conteos, long total) {
        return conteos.stream()
                .map(c -> List.of(c.etiqueta(), String.valueOf(c.cantidad()),
                        total == 0 ? "" : String.format(Locale.ROOT, "%.1f %%", c.cantidad() * 100.0 / total)))
                .toList();
    }

    private static String horario(EventoResponse e) {
        if (e.horaInicio() == null) {
            return "";
        }
        return e.horaFin() == null ? HORA.format(e.horaInicio())
                : HORA.format(e.horaInicio()) + " a " + HORA.format(e.horaFin());
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor;
    }
}
