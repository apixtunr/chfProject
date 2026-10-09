package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.common.reporte.ReporteTabla;
import com.lacasadelchef.erp.common.reporte.ReporteTablaBuilder;
import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.Pago;
import com.lacasadelchef.erp.pago.CuentaPorCobrar.Situacion;
import com.lacasadelchef.erp.repository.PagoRepository;
import com.lacasadelchef.erp.repository.VPagoEventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Reportes del modulo de Pagos: recibos emitidos y cuentas por cobrar. */
@Service
@RequiredArgsConstructor
public class ReportePagoService {

    private static final String ESTADO_ANULADO = "ANULADO";

    private final PagoRepository pagoRepository;
    private final VPagoEventoRepository vPagoEventoRepository;

    /**
     * Recibos emitidos en el periodo, en su secuencia. Los anulados aparecen (para que no
     * queden huecos en la numeracion) pero no suman.
     */
    @Transactional(readOnly = true)
    public ReporteTabla recibos(LocalDate desde, LocalDate hasta, Integer idMetodoPago, Integer idEstado, String agrupar) {
        List<Pago> pagos = pagoRepository.recibosEmitidos(
                desde == null ? null : desde.atStartOfDay(),
                hasta == null ? null : hasta.plusDays(1).atStartOfDay(),
                idMetodoPago, idEstado);
        return ReporteTablaBuilder.de("Recibos emitidos", pagos)
                .texto("recibo", "Recibo", 1.2f, p -> ReciboPagoPdfService.numeroRecibo(p.getIdPago()))
                .fechaHora("fecha", "Fecha", 1.7f, Pago::getFechaPago)
                .texto("evento", "Evento", 1.6f,
                        p -> "#" + p.getEvento().getIdEvento() + " " + p.getEvento().getTipoEvento().getNombreTipo())
                .texto("cliente", "Cliente", 2.3f, p -> nombreCliente(p.getEvento()))
                .texto("concepto", "Concepto", 1.7f, ReportePagoService::concepto)
                .texto("metodo", "Forma de pago", 1.4f, p -> p.getMetodoPago().getNombreMetodo())
                .texto("referencia", "Referencia", 1.3f, Pago::getReferenciaTransaccion)
                .texto("recibidoPor", "Recibido por", 1.3f, p -> p.getUsuario() != null ? p.getUsuario().getUsername() : "")
                .texto("estado", "Estado", 1.6f, p -> p.getEstado().getNombre())
                .monto("monto", "Monto", 1.2f, true, Pago::getMonto)
                .fechaDeAgrupacion(p -> p.getFechaPago().toLocalDate())
                .agrupacion("METODO", p -> p.getMetodoPago().getNombreMetodo())
                .excluirDeTotales(p -> ESTADO_ANULADO.equalsIgnoreCase(p.getEstado().getNombre()))
                .construir(agrupar);
    }

    /**
     * Eventos con saldo pendiente (sin los cancelados), con lo que les toca pagar ahora y
     * para cuando. Las fechas filtran por la fecha del evento.
     */
    @Transactional(readOnly = true)
    public ReporteTabla cuentasPorCobrar(LocalDate desde, LocalDate hasta, Integer idCliente, Situacion situacion,
                                         String agrupar) {
        LocalDate hoy = LocalDate.now();
        List<CuentaPorCobrar> cuentas = vPagoEventoRepository.conSaldo(idCliente).stream()
                .filter(v -> desde == null || !v.getFechaEvento().isBefore(desde))
                .filter(v -> hasta == null || !v.getFechaEvento().isAfter(hasta))
                .map(v -> CuentaPorCobrar.de(v, hoy))
                .filter(c -> situacion == null || c.situacion() == situacion)
                .toList();
        return ReporteTablaBuilder.de("Cuentas por cobrar", cuentas)
                .texto("evento", "Evento", 0.9f, c -> "#" + c.evento().getIdEvento())
                .fecha("fechaEvento", "Fecha del evento", 1.2f, c -> c.evento().getFechaEvento())
                .texto("cliente", "Cliente", 2.4f, c -> c.evento().getClienteNombre())
                .texto("tipo", "Tipo", 1.3f, c -> c.evento().getTipoEventoNombre())
                .monto("total", "Total", 1.2f, true, c -> c.evento().getTotal())
                .monto("abonado", "Abonado", 1.2f, true, c -> c.evento().getAbonado())
                .monto("pendiente", "Pendiente", 1.2f, true, c -> c.evento().getPendiente())
                .monto("faltaAnticipo", "Falta del anticipo", 1.2f, true, CuentaPorCobrar::faltaAnticipo)
                .texto("situacion", "Situación", 1.4f, c -> c.situacion().etiqueta())
                .texto("vence", "Vence", 1.3f, CuentaPorCobrar::textoVencimiento)
                .fechaDeAgrupacion(c -> c.evento().getFechaEvento())
                .agrupacion("SITUACION", c -> c.situacion().etiqueta())
                .agrupacion("CLIENTE", c -> c.evento().getClienteNombre())
                .construir(agrupar);
    }

    private static String concepto(Pago pago) {
        return pago.getCostoEvento() != null
                ? "Reembolso: " + pago.getCostoEvento().getTipoCosto().getNombreTipo()
                : "Abono";
    }

    private static String nombreCliente(Evento evento) {
        Cliente cliente = evento.getCotizacionVersion() != null
                ? evento.getCotizacionVersion().getCotizacion().getCliente()
                : evento.getCliente();
        return cliente != null ? cliente.getNombre() : "";
    }
}
