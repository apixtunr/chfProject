package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.common.pdf.ReportePdf;
import com.lacasadelchef.erp.common.pdf.ReportePdf.Columna;
import com.lacasadelchef.erp.entity.Bebida;
import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.Plato;
import com.lacasadelchef.erp.entity.UnidadVenta;
import com.lacasadelchef.erp.entity.Ubicacion;
import com.lacasadelchef.erp.entity.Vehiculo;
import com.lacasadelchef.erp.repository.DetalleCotizacionRepository;
import com.lacasadelchef.erp.repository.DetalleEventoRepository;
import com.lacasadelchef.erp.repository.EventoEmpleadoRepository;
import com.lacasadelchef.erp.repository.EventoInventarioRepository;
import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.EventoVehiculoRepository;
import com.lacasadelchef.erp.repository.ServicioCotizacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.lacasadelchef.erp.common.pdf.ReportePdf.fecha;

/**
 * Hoja de servicio de un evento: lo que el personal necesita tener a mano el dia del
 * evento (donde y a que hora, que se sirve, quien va, en que vehiculo y que insumos se
 * cargan). Reemplaza la hoja que se anotaba a mano.
 *
 * No incluye montos: la leen Cocina y Almacén, que no tienen acceso a los costos ni a lo
 * que se le paga a cada persona.
 */
@Service
@RequiredArgsConstructor
public class HojaServicioPdfService {

    private final EventoRepository eventoRepository;
    private final DetalleCotizacionRepository detalleCotizacionRepository;
    private final DetalleEventoRepository detalleEventoRepository;
    private final ServicioCotizacionRepository servicioCotizacionRepository;
    private final EventoEmpleadoRepository eventoEmpleadoRepository;
    private final EventoVehiculoRepository eventoVehiculoRepository;
    private final EventoInventarioRepository eventoInventarioRepository;

    /** Una linea del menu, venga de la cotizacion aceptada o del menu directo del evento. */
    private record LineaMenu(String menu, Plato plato, Integer cantidad, Bebida bebida, String observaciones) {
    }

    @Transactional(readOnly = true)
    public byte[] generar(Integer idEvento) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", idEvento));
        Cliente cliente = evento.getCotizacionVersion() != null
                ? evento.getCotizacionVersion().getCotizacion().getCliente()
                : evento.getCliente();

        ReportePdf pdf = ReportePdf.carta()
                .titulo("HOJA DE SERVICIO — EVENTO #" + evento.getIdEvento(),
                        evento.getTipoEvento().getNombreTipo() + "  ·  Estado: " + evento.getEstado().getNombre());

        List<String[]> datos = new ArrayList<>();
        datos.add(new String[] {"Fecha", fecha(evento.getFechaEvento())});
        datos.add(new String[] {"Horario", horario(evento.getHoraInicio(), evento.getHoraFin())});
        datos.add(new String[] {"Invitados", evento.getCantidadPersonas() == null
                ? "—" : evento.getCantidadPersonas() + " personas"});
        datos.add(new String[] {"Cliente", cliente != null ? cliente.getNombre() : "—"});
        datos.add(new String[] {"Teléfono del cliente", cliente != null && cliente.getTelefono() != null
                ? cliente.getTelefono() : "—"});
        datos.add(new String[] {"Ubicación", ubicacion(evento.getUbicacion())});
        pdf.datos(3, datos);

        if (evento.getObservaciones() != null && !evento.getObservaciones().isBlank()) {
            pdf.seccion("Observaciones");
            pdf.texto(evento.getObservaciones());
        }

        pdf.seccion("Menú");
        pdf.tabla(List.of(Columna.texto("Plato", 3.2f), Columna.texto("Menú", 1.8f), Columna.texto("Cantidad", 1.2f),
                        Columna.texto("Bebida", 1.4f), Columna.texto("Observaciones", 2f)),
                menu(evento).stream()
                        .map(l -> List.of(l.plato().getNombrePlato(), l.menu(), cantidad(l.plato(), l.cantidad()),
                                l.bebida() != null ? l.bebida().getNombreBebida() : "—",
                                l.observaciones() == null ? "" : l.observaciones()))
                        .toList(),
                null, "El evento no tiene menú registrado");

        if (evento.getCotizacionVersion() != null) {
            List<List<String>> servicios = servicioCotizacionRepository
                    .findByCotizacionVersionIdCotizacionVersion(evento.getCotizacionVersion().getIdCotizacionVersion())
                    .stream()
                    .map(s -> List.of(s.getTipoServicio().getNombreTipo(), s.getDescripcion() == null ? "" : s.getDescripcion()))
                    .toList();
            if (!servicios.isEmpty()) {
                pdf.seccion("Servicios extra");
                pdf.tabla(List.of(Columna.texto("Servicio", 2f), Columna.texto("Detalle", 4f)), servicios, null, "");
            }
        }

        pdf.seccion("Personal");
        pdf.tabla(List.of(Columna.texto("Nombre", 3f), Columna.texto("Puesto", 2f), Columna.texto("Teléfono", 1.4f),
                        Columna.texto("Horario", 1.4f)),
                eventoEmpleadoRepository.findByEventoIdEvento(idEvento).stream()
                        .sorted(Comparator.comparing(ee -> ee.getEmpleado().getNombreCompleto()))
                        .map(ee -> {
                            Empleado e = ee.getEmpleado();
                            return List.of(e.getNombreCompleto(),
                                    e.getPuestoEmpleado() != null ? e.getPuestoEmpleado().getNombreRol() : "",
                                    e.getTelefono() == null ? "" : e.getTelefono(),
                                    horario(ee.getHoraInicio() != null ? ee.getHoraInicio() : evento.getHoraInicio(),
                                            ee.getHoraFin() != null ? ee.getHoraFin() : evento.getHoraFin()));
                        })
                        .toList(),
                null, "El evento no tiene personal asignado");

        pdf.seccion("Vehículos");
        pdf.tabla(List.of(Columna.texto("Vehículo", 3f), Columna.texto("Placa", 1.4f), Columna.texto("Conductor", 3f)),
                eventoVehiculoRepository.findByEventoIdEvento(idEvento).stream()
                        .map(ev -> List.of(vehiculo(ev.getVehiculo()), ev.getVehiculo().getPlaca(),
                                ev.getEmpleado() != null ? ev.getEmpleado().getNombreCompleto() : "—"))
                        .toList(),
                null, "El evento no tiene vehículos asignados");

        pdf.seccion("Insumos");
        pdf.tabla(List.of(Columna.texto("Producto", 4f), Columna.monto("Cantidad", 1.2f), Columna.texto("Unidad", 1.4f)),
                eventoInventarioRepository.findByEventoIdEvento(idEvento).stream()
                        .sorted(Comparator.comparing(ei -> ei.getProducto().getNombreProducto()))
                        .map(ei -> List.of(ei.getProducto().getNombreProducto(), numero(ei.getCantidad()),
                                ei.getProducto().getUnidadMedida()))
                        .toList(),
                null, "El evento no tiene insumos registrados");

        return pdf.cerrar();
    }

    private List<LineaMenu> menu(Evento evento) {
        if (evento.getCotizacionVersion() != null) {
            return detalleCotizacionRepository
                    .findByCotizacionVersionIdCotizacionVersion(evento.getCotizacionVersion().getIdCotizacionVersion())
                    .stream()
                    .map(d -> new LineaMenu(d.getMenu().getNombreMenu(), d.getPlato(), d.getCantidadPlatos(),
                            d.getBebida(), d.getObservaciones()))
                    .toList();
        }
        return detalleEventoRepository.findByEventoIdEvento(evento.getIdEvento()).stream()
                .map(d -> new LineaMenu(d.getMenu().getNombreMenu(), d.getPlato(), d.getCantidadPlatos(),
                        d.getBebida(), d.getObservaciones()))
                .toList();
    }

    /** "120 personas" para un plato por persona; "300 unidades" para las boquitas y piezas. */
    static String cantidad(Plato plato, Integer cantidad) {
        int n = cantidad == null ? 0 : cantidad;
        if (plato.getUnidadVenta() == UnidadVenta.PERSONA) {
            return n + (n == 1 ? " persona" : " personas");
        }
        return n + (n == 1 ? " unidad" : " unidades");
    }

    private static String ubicacion(Ubicacion ubicacion) {
        if (ubicacion == null) {
            return "—";
        }
        String lugar = ubicacion.getDireccion();
        if (ubicacion.getMunicipio() != null) {
            lugar += ", " + ubicacion.getMunicipio().getNombreMunicipio();
            if (ubicacion.getMunicipio().getDepartamento() != null) {
                lugar += ", " + ubicacion.getMunicipio().getDepartamento().getNombreDepartamento();
            }
        }
        return lugar;
    }

    private static String vehiculo(Vehiculo v) {
        if (v.getLineaVehiculo() == null) {
            return "";
        }
        String marca = v.getLineaVehiculo().getMarcaVehiculo() != null
                ? v.getLineaVehiculo().getMarcaVehiculo().getNombreMarca() + " " : "";
        return marca + v.getLineaVehiculo().getNombreLinea()
                + (v.getAnioVehiculo() != null ? " " + v.getAnioVehiculo() : "");
    }

    private static String horario(LocalTime inicio, LocalTime fin) {
        if (inicio == null || fin == null) {
            return "—";
        }
        return "%s a %s".formatted(inicio.toString().substring(0, 5), fin.toString().substring(0, 5));
    }

    /** 12 en vez de 12.00; 2.5 se queda como 2.5. */
    private static String numero(BigDecimal valor) {
        return valor == null ? "0" : valor.stripTrailingZeros().toPlainString();
    }
}
