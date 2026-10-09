package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.reporte.ReporteTabla;
import com.lacasadelchef.erp.common.reporte.ReporteTablaBuilder;
import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.EventoRepository.EventoReporteFila;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

/**
 * Reporte de eventos por periodo. No lleva montos: el modulo de Eventos lo consultan
 * tambien Cocina y Almacén, que no ven los costos ni los precios.
 */
@Service
@RequiredArgsConstructor
public class EventoReporteService {

    /** Filtrar y agrupar por el dia en que se registro el evento, en lugar de su fecha. */
    public static final String SEGUN_REGISTRO = "REGISTRO";

    private final EventoRepository eventoRepository;

    @Transactional(readOnly = true)
    public ReporteTabla eventos(String fechaSegun, LocalDate desde, LocalDate hasta, Integer idEstado,
                                Integer idTipoEvento, Integer idCliente, String agrupar) {
        List<EventoReporteFila> eventos = eventoRepository.reporteEventos(
                fechaSegun == null ? "EVENTO" : fechaSegun, desde, hasta, idEstado, idTipoEvento, idCliente);
        Function<EventoReporteFila, LocalDate> fecha = SEGUN_REGISTRO.equals(fechaSegun)
                ? e -> e.getFechaRegistro().toLocalDate()
                : EventoReporteFila::getFechaEvento;
        return ReporteTablaBuilder.de("Eventos por periodo", eventos)
                .texto("evento", "Evento", 0.7f, e -> "#" + e.getIdEvento())
                .fecha("registro", "Registrado", 1.1f, e -> e.getFechaRegistro().toLocalDate())
                .fecha("fechaEvento", "Fecha del evento", 1.2f, EventoReporteFila::getFechaEvento)
                .texto("horario", "Horario", 1.1f, EventoReporteService::horario)
                .texto("cliente", "Cliente", 2.4f, EventoReporteFila::getCliente)
                .texto("tipo", "Tipo", 1.4f, EventoReporteFila::getTipoEvento)
                .numero("personas", "Personas", 0.9f, true, EventoReporteFila::getPersonas)
                .texto("estado", "Estado", 1.2f, EventoReporteFila::getEstado)
                .fechaDeAgrupacion(fecha)
                .agrupacion("ESTADO", EventoReporteFila::getEstado)
                .agrupacion("TIPO", EventoReporteFila::getTipoEvento)
                .construir(agrupar);
    }

    private static String horario(EventoReporteFila e) {
        if (e.getHoraInicio() == null || e.getHoraFin() == null) {
            return "";
        }
        return "%s a %s".formatted(e.getHoraInicio().toString().substring(0, 5), e.getHoraFin().toString().substring(0, 5));
    }
}
