package com.lacasadelchef.erp.rentabilidad;

import com.lacasadelchef.erp.common.reporte.ReporteTabla;
import com.lacasadelchef.erp.common.reporte.ReporteTablaBuilder;
import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.EventoEmpleado;
import com.lacasadelchef.erp.repository.EventoEmpleadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class PagoPersonalService {

    private final EventoEmpleadoRepository eventoEmpleadoRepository;

    @Transactional(readOnly = true)
    public ReporteTabla generar(LocalDate fechaDesde, LocalDate fechaHasta, String agrupar) {
        return ReporteTablaBuilder.de("Reporte de Pagos al Personal",
                        eventoEmpleadoRepository.pagosEnFinalizados(fechaDesde, fechaHasta, null))
                .texto("nombre", "Nombre", 2.4f, ee -> ee.getEmpleado().getNombreCompleto())
                .texto("puesto", "Puesto", 1.4f, ee -> ee.getEmpleado().getPuestoEmpleado() != null
                        ? ee.getEmpleado().getPuestoEmpleado().getNombreRol() : "")
                .texto("evento", "Evento", 0.9f, ee -> "#" + ee.getEvento().getIdEvento())
                .fecha("fecha", "Fecha", 1f, ee -> ee.getEvento().getFechaEvento())
                .texto("tipo", "Tipo", 1.3f, ee -> ee.getEvento().getTipoEvento().getNombreTipo())
                .texto("cliente", "Cliente", 2.2f, ee -> nombreCliente(ee.getEvento()))
                .texto("horario", "Horario", 1.2f, PagoPersonalService::horario)
                .monto("monto", "Monto", 1.1f, true, EventoEmpleado::getSalarioEvento)
                .fechaDeAgrupacion(ee -> ee.getEvento().getFechaEvento())
                .agrupacion("PERSONA", ee -> ee.getEmpleado().getNombreCompleto())
                .construir(agrupar);
    }

    private static String horario(EventoEmpleado ee) {
        LocalTime inicio = ee.getHoraInicio() != null ? ee.getHoraInicio() : ee.getEvento().getHoraInicio();
        LocalTime fin = ee.getHoraFin() != null ? ee.getHoraFin() : ee.getEvento().getHoraFin();
        if (inicio == null || fin == null) {
            return "";
        }
        return "%s a %s".formatted(inicio.toString().substring(0, 5), fin.toString().substring(0, 5));
    }

    private static String nombreCliente(Evento evento) {
        Cliente cliente = evento.getCotizacionVersion() != null
                ? evento.getCotizacionVersion().getCotizacion().getCliente()
                : evento.getCliente();
        return cliente != null ? cliente.getNombre() : "";
    }
}
