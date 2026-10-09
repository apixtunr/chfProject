package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.cotizacion.CondicionesComerciales;
import com.lacasadelchef.erp.cotizacion.dto.CotizacionResponse;
import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.entity.CotizacionVersion;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.TipoEvento;
import com.lacasadelchef.erp.entity.Ubicacion;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.entity.VPagoEvento;
import com.lacasadelchef.erp.evento.dto.AnticipoResponse;
import com.lacasadelchef.erp.evento.dto.CancelarEventoRequest;
import com.lacasadelchef.erp.evento.dto.ConteoResponse;
import com.lacasadelchef.erp.evento.dto.EventoRequest;
import com.lacasadelchef.erp.evento.dto.EventoResponse;
import com.lacasadelchef.erp.evento.dto.EventoResumenResponse;
import com.lacasadelchef.erp.repository.ClienteRepository;
import com.lacasadelchef.erp.repository.CotizacionVersionRepository;
import com.lacasadelchef.erp.repository.DetalleEventoRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.EventoEmpleadoRepository;
import com.lacasadelchef.erp.repository.EventoInventarioRepository;
import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.EventoVehiculoRepository;
import com.lacasadelchef.erp.repository.TipoEventoRepository;
import com.lacasadelchef.erp.repository.UbicacionRepository;
import com.lacasadelchef.erp.repository.VPagoEventoRepository;
import com.lacasadelchef.erp.security.UsuarioPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EventoServiceImpl implements EventoService {

    private static final String TIPO_ESTADO_EVENTO = "EVENTO";
    private static final String ESTADO_CREADO = "CREADO";
    private static final String ESTADO_PLANIFICADO = "PLANIFICADO";
    private static final String ESTADO_EN_CURSO = "EN CURSO";
    private static final String ESTADO_CANCELADO = "CANCELADO";
    private static final String ESTADO_COTIZACION_ACEPTADA = "ACEPTADA";
    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";
    private static final String ACUERDO_SIN_ANTICIPO = "SIN_ANTICIPO";
    private static final String ACUERDO_RETENIDO = "RETENIDO";
    private static final String ACUERDO_DEVUELTO = "DEVUELTO";
    private static final String ACUERDO_DEVUELTO_PARCIAL = "DEVUELTO_PARCIAL";

    // EN CURSO y FINALIZADO los pone solo EventoEstadoAutomaticoJob/EventoEstadoSchedulerService,
    // en base a la fecha/hora cargada; CANCELADO es la unica transicion que un usuario puede
    // disparar a mano via cambiarEstado(). CREADO -> PLANIFICADO tiene su propio metodo
    // (planificar()) porque necesita validar que el evento ya este completo, algo que
    // cambiarEstado() no hace para ningun otro caso.
    private static final Map<String, Set<String>> TRANSICIONES_VALIDAS = Map.of(
            ESTADO_CREADO, Set.of(ESTADO_CANCELADO),
            ESTADO_PLANIFICADO, Set.of(ESTADO_CANCELADO),
            ESTADO_EN_CURSO, Set.of(ESTADO_CANCELADO));

    private final EventoRepository eventoRepository;
    private final CotizacionVersionRepository cotizacionVersionRepository;
    private final ClienteRepository clienteRepository;
    private final TipoEventoRepository tipoEventoRepository;
    private final UbicacionRepository ubicacionRepository;
    private final EstadoRepository estadoRepository;
    private final DetalleEventoRepository detalleEventoRepository;
    private final EventoEmpleadoRepository eventoEmpleadoRepository;
    private final EventoVehiculoRepository eventoVehiculoRepository;
    private final EventoInventarioRepository eventoInventarioRepository;
    private final EventoEstadoSchedulerService estadoSchedulerService;
    private final VPagoEventoRepository vPagoEventoRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<EventoResponse> listar(LocalDate fechaDesde, LocalDate fechaHasta, Integer idCliente,
                                        Integer idTipoEvento, Integer idEstado, Pageable pageable) {
        return eventoRepository.buscarPorFiltros(fechaDesde, fechaHasta, idCliente, idTipoEvento, idEstado, pageable)
                .map(EventoResponse::desde);
    }

    @Override
    @Transactional(readOnly = true)
    public EventoResumenResponse resumen(LocalDate fechaDesde, LocalDate fechaHasta, Integer idCliente, Integer idTipoEvento) {
        long total = eventoRepository.buscarPorFiltros(fechaDesde, fechaHasta, idCliente, idTipoEvento, null, Pageable.unpaged())
                .getTotalElements();
        List<ConteoResponse> porEstado = eventoRepository.contarPorEstado(fechaDesde, fechaHasta, idCliente, idTipoEvento)
                .stream()
                .map(c -> ConteoResponse.builder().etiqueta(c.getEtiqueta()).cantidad(c.getCantidad()).build())
                .toList();
        List<ConteoResponse> porTipo = eventoRepository.contarPorTipo(fechaDesde, fechaHasta, idCliente, null)
                .stream()
                .map(c -> ConteoResponse.builder().etiqueta(c.getEtiqueta()).cantidad(c.getCantidad()).build())
                .toList();
        return EventoResumenResponse.builder()
                .totalEventos(total)
                .porEstado(porEstado)
                .porTipo(porTipo)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EventoResponse obtenerPorId(Integer id) {
        return EventoResponse.desde(buscarEvento(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CotizacionResponse> listarCotizacionesDisponibles() {
        return cotizacionVersionRepository.buscarAceptadasSinEvento().stream()
                .map(cv -> CotizacionResponse.desde(cv.getCotizacion(), cv))
                .toList();
    }

    @Override
    @Transactional
    public EventoResponse crear(EventoRequest request) {
        Evento evento = new Evento();
        aplicar(request, evento);
        validarAnticipacion(evento);
        evento.setEstado(buscarEstado(TIPO_ESTADO_EVENTO, ESTADO_CREADO));
        evento = eventoRepository.save(evento);
        estadoSchedulerService.programar(evento);
        return EventoResponse.desde(evento);
    }

    @Override
    @Transactional
    public EventoResponse actualizar(Integer id, EventoRequest request) {
        Evento evento = buscarEvento(id);
        EventoReglas.validarModificable(evento, "modificar");
        aplicar(request, evento);
        evento = eventoRepository.save(evento);
        estadoSchedulerService.programar(evento);
        return EventoResponse.desde(evento);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        // Si el evento tiene costos, pagos o asignaciones, la FK lo impide y el
        // GlobalExceptionHandler lo traduce a HTTP 409.
        Evento evento = buscarEvento(id);
        eventoRepository.delete(evento);
        estadoSchedulerService.cancelarTareas(id);
    }

    @Override
    @Transactional
    public EventoResponse cambiarEstado(Integer id, Integer idEstado) {
        Evento evento = buscarEvento(id);
        Estado estado = estadoRepository.findById(idEstado)
                .orElseThrow(() -> new ResourceNotFoundException("Estado", idEstado));
        if (!TIPO_ESTADO_EVENTO.equalsIgnoreCase(estado.getTipoEstado().getNombreTipo())) {
            throw new BusinessException(
                    "El estado %s no pertenece al catalogo de eventos".formatted(estado.getNombre()));
        }

        String estadoActual = evento.getEstado().getNombre().toUpperCase();
        String estadoDestino = estado.getNombre().toUpperCase();
        Set<String> permitidos = TRANSICIONES_VALIDAS.get(estadoActual);
        if (permitidos == null || !permitidos.contains(estadoDestino)) {
            throw new BusinessException(
                    "No se puede pasar el evento de %s a %s".formatted(estadoActual, estadoDestino));
        }
        if (ESTADO_CANCELADO.equals(estadoDestino)) {
            throw new BusinessException("Para cancelar un evento use la opción Cancelar, que pide el motivo y el"
                    + " acuerdo con el cliente");
        }

        evento.setEstado(estado);
        evento = eventoRepository.save(evento);
        // Unica transicion manual (ver TRANSICIONES_VALIDAS): siempre termina en CANCELADO,
        // que no tiene ningun temporizador pendiente que programar, solo cancelar el que haya.
        estadoSchedulerService.cancelarTareas(evento.getIdEvento());
        return EventoResponse.desde(evento);
    }

    @Override
    @Transactional
    public EventoResponse planificar(Integer id, boolean sinAnticipo) {
        Evento evento = buscarEvento(id);
        if (!ESTADO_CREADO.equalsIgnoreCase(evento.getEstado().getNombre())) {
            throw new BusinessException(
                    "Solo un evento en estado CREADO se puede planificar (actual: %s)"
                            .formatted(evento.getEstado().getNombre()));
        }

        List<String> faltantes = new ArrayList<>();
        // El menu de un evento con cotizacion ya viene garantizado desde que se acepto esa
        // version (no se vuelve a pedir aqui); solo un evento directo puede tener el menu vacio.
        if (evento.getCotizacionVersion() == null && !detalleEventoRepository.existsByEventoIdEvento(id)) {
            faltantes.add("Menú");
        }
        if (!eventoEmpleadoRepository.existsByEventoIdEvento(id)) {
            faltantes.add("Personal");
        }
        if (!eventoVehiculoRepository.existsByEventoIdEvento(id)) {
            faltantes.add("Vehículos");
        }
        if (!eventoInventarioRepository.existsByEventoIdEvento(id)) {
            faltantes.add("Inventario");
        }
        if (!faltantes.isEmpty()) {
            throw new BusinessException(
                    "Para planificar el evento primero hay que completar: %s".formatted(String.join(", ", faltantes)));
        }

        // El 50% se cobra una semana antes: planificar sin el queda a criterio del usuario,
        // que tiene que confirmarlo (sinAnticipo); se marca en el evento y la bitacora guarda
        // quien lo hizo.
        AnticipoResponse anticipo = anticipo(id);
        if (!anticipo.cubierto() && !sinAnticipo) {
            throw new BusinessException(("El cliente ha pagado Q%s de Q%s; falta Q%s para el 50%% que se cobra una"
                    + " semana antes del evento. Confirme si desea planificarlo de todos modos.")
                    .formatted(anticipo.abonado(), anticipo.total(), anticipo.faltante()));
        }
        evento.setPlanificadoSinAnticipo(!anticipo.cubierto());

        evento.setEstado(buscarEstado(TIPO_ESTADO_EVENTO, ESTADO_PLANIFICADO));
        evento = eventoRepository.save(evento);
        // Recien aqui entra a la automatizacion: se programa el temporizador exacto de inicio.
        estadoSchedulerService.programar(evento);
        return EventoResponse.desde(evento);
    }

    @Override
    @Transactional(readOnly = true)
    public AnticipoResponse anticipo(Integer id) {
        buscarEvento(id);
        VPagoEvento pagos = vPagoEventoRepository.findById(id).orElse(null);
        BigDecimal total = (pagos == null || pagos.getTotal() == null ? BigDecimal.ZERO : pagos.getTotal())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal abonado = (pagos == null || pagos.getAbonado() == null ? BigDecimal.ZERO : pagos.getAbonado())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal requerido = total.multiply(CondicionesComerciales.PORCION_ANTICIPO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal faltante = requerido.subtract(abonado).max(BigDecimal.ZERO);
        return new AnticipoResponse(total, abonado, requerido, faltante, faltante.signum() == 0);
    }

    @Override
    @Transactional
    public EventoResponse cancelar(Integer id, CancelarEventoRequest request) {
        Evento evento = buscarEvento(id);
        EventoReglas.validarModificable(evento, "cancelar");
        if (!ROL_ADMINISTRADOR.equalsIgnoreCase(rolUsuarioActual())) {
            throw new BusinessException("Solo un administrador puede cancelar un evento");
        }

        // No hay una politica fija de devolucion: se negocia con el cliente y aqui se
        // registra en que quedo lo pagado. Si se devuelve, los pagos se anulan en Pagos.
        BigDecimal abonado = anticipo(id).abonado();
        String acuerdo = request.acuerdoAnticipo() == null ? null : request.acuerdoAnticipo().trim().toUpperCase();
        BigDecimal devuelto;
        if (abonado.signum() == 0) {
            acuerdo = ACUERDO_SIN_ANTICIPO;
            devuelto = null;
        } else if (ACUERDO_RETENIDO.equals(acuerdo)) {
            devuelto = BigDecimal.ZERO;
        } else if (ACUERDO_DEVUELTO.equals(acuerdo)) {
            devuelto = abonado;
        } else if (ACUERDO_DEVUELTO_PARCIAL.equals(acuerdo)) {
            devuelto = request.montoDevuelto();
            if (devuelto == null || devuelto.signum() <= 0 || devuelto.compareTo(abonado) >= 0) {
                throw new BusinessException(
                        "Indique cuánto se devuelve: más de Q0.00 y menos de lo pagado (Q%s)".formatted(abonado));
            }
        } else {
            throw new BusinessException(("El cliente ha pagado Q%s: indique si se retiene, se devuelve todo o se"
                    + " devuelve una parte").formatted(abonado));
        }

        evento.setMotivoCancelacion(request.motivo().trim());
        evento.setAcuerdoAnticipo(acuerdo);
        evento.setMontoDevuelto(devuelto);
        evento.setEstado(buscarEstado(TIPO_ESTADO_EVENTO, ESTADO_CANCELADO));
        evento = eventoRepository.save(evento);
        // Ya no inicia ni finaliza solo. Lo planificado del inventario no se habia descontado
        // (solo se descuenta al iniciar) y un evento cancelado ya no cuenta en los faltantes.
        estadoSchedulerService.cancelarTareas(evento.getIdEvento());
        return EventoResponse.desde(evento);
    }

    private Evento buscarEvento(Integer id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", id));
    }

    private Estado buscarEstado(String tipoEstado, String nombre) {
        return estadoRepository.findByTipoEstadoNombreTipoAndNombre(tipoEstado, nombre)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el estado %s/%s (revisar datos semilla)".formatted(tipoEstado, nombre)));
    }

    private void aplicar(EventoRequest request, Evento evento) {
        if (request.idCotizacionVersion() != null) {
            CotizacionVersion cotizacionVersion = cotizacionVersionRepository.findById(request.idCotizacionVersion())
                    .orElseThrow(() -> new ResourceNotFoundException("CotizacionVersion", request.idCotizacionVersion()));
            if (!ESTADO_COTIZACION_ACEPTADA.equalsIgnoreCase(cotizacionVersion.getEstado().getNombre())) {
                throw new BusinessException(
                        "Solo se puede crear un evento a partir de una version de cotizacion ACEPTADA (actual: %s)"
                                .formatted(cotizacionVersion.getEstado().getNombre()));
            }
            Integer idEventoActual = evento.getIdEvento() != null ? evento.getIdEvento() : -1;
            if (eventoRepository.existsByCotizacionVersionIdCotizacionVersionAndIdEventoNot(
                    request.idCotizacionVersion(), idEventoActual)) {
                throw new BusinessException("Esta version de cotizacion ya tiene un evento asociado");
            }

            // Lo que el cliente acepto en la cotizacion es lo que lleva el evento:
            // tipo, ubicacion, fecha y cantidad de personas no se vuelven a pedir.
            var cotizacion = cotizacionVersion.getCotizacion();
            evento.setCotizacionVersion(cotizacionVersion);
            evento.setCliente(null);
            evento.setTipoEvento(cotizacion.getTipoEvento());
            evento.setUbicacion(cotizacion.getUbicacion());
            evento.setFechaEvento(cotizacion.getFechaEvento());
            evento.setCantidadPersonas(cotizacion.getCantidadPersonas());
        } else {
            if (request.idCliente() == null) {
                throw new BusinessException(
                        "Un evento sin cotizacion debe indicar el cliente directamente (idCliente)");
            }
            if (request.idTipoEvento() == null || request.idUbicacion() == null
                    || request.fechaEvento() == null || request.cantidadPersonas() == null) {
                throw new BusinessException(
                        "Un evento sin cotizacion debe indicar tipo de evento, ubicacion, fecha y cantidad de personas");
            }
            Cliente cliente = clienteRepository.findById(request.idCliente())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.idCliente()));
            boolean clienteNuevo = evento.getCliente() == null
                    || !evento.getCliente().getIdCliente().equals(cliente.getIdCliente());
            if (clienteNuevo && !cliente.estaActivo()) {
                throw new BusinessException("El cliente %s está inactivo: reactívelo antes de %s".formatted(cliente.getNombre(), "crearle un evento"));
            }
            TipoEvento tipoEvento = tipoEventoRepository.findById(request.idTipoEvento())
                    .orElseThrow(() -> new ResourceNotFoundException("TipoEvento", request.idTipoEvento()));
            Ubicacion ubicacion = ubicacionRepository.findById(request.idUbicacion())
                    .orElseThrow(() -> new ResourceNotFoundException("Ubicacion", request.idUbicacion()));

            evento.setCliente(cliente);
            evento.setCotizacionVersion(null);
            evento.setTipoEvento(tipoEvento);
            evento.setUbicacion(ubicacion);
            evento.setFechaEvento(request.fechaEvento());
            evento.setCantidadPersonas(request.cantidadPersonas());
        }

        // El horario sigue las mismas reglas que la cotizacion, venga o no de una: empieza en
        // punto entre las 7:00 y las 19:00 y el fin lo calcula el sistema (4 horas, hasta
        // las 21:00 o las 22:00; un desayuno, hasta las 11:00). Si viene de una cotizacion y no se indica otro, el acordado.
        LocalTime inicio = request.horaInicio();
        var cotizacion = evento.getCotizacionVersion() == null ? null : evento.getCotizacionVersion().getCotizacion();
        if (inicio == null && cotizacion != null) {
            inicio = cotizacion.getHoraInicio();
        }
        if (inicio == null) {
            throw new BusinessException("Indique el horario del servicio");
        }
        boolean cambiaElHorario = !inicio.equals(evento.getHoraInicio());
        if (cambiaElHorario && !CondicionesComerciales.esHoraDeInicioPermitida(inicio)) {
            throw new BusinessException("El servicio empieza en punto entre las 7:00 y las 19:00");
        }
        evento.setHoraInicio(inicio);
        evento.setHoraFin(CondicionesComerciales.horaFinServicio(inicio));
        evento.setObservaciones(request.observaciones());
    }

    // Una semana de anticipacion (el 50% se cobra una semana antes). Solo aplica al crear:
    // un evento existente puede quedar a menos de 7 dias, o ya haber pasado, sin que eso
    // impida registrarle cosas.
    private void validarAnticipacion(Evento evento) {
        LocalDate minima = CondicionesComerciales.fechaMinimaEvento(LocalDate.now());
        if (evento.getFechaEvento() == null || evento.getFechaEvento().isBefore(minima)) {
            throw new BusinessException("Los eventos se agendan con al menos %d días de anticipación: la fecha más"
                    .formatted(CondicionesComerciales.DIAS_ANTICIPACION)
                    + " próxima es el %s".formatted(minima.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        }
        // Un evento para hoy no puede empezar a una hora que ya paso: nunca se llegaria a
        // planificar y el proceso automatico lo cancelaria en minutos. Solo ocurre con el
        // modo pruebas (sin anticipacion), pero la regla vale siempre.
        if (evento.getFechaEvento().equals(LocalDate.now()) && evento.getHoraInicio() != null
                && !evento.getHoraInicio().isAfter(LocalTime.now())) {
            throw new BusinessException("La hora de inicio (%s) ya pasó: elija una hora posterior a la actual"
                    .formatted(evento.getHoraInicio()));
        }
    }

    private String rolUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal principal) {
            Usuario usuario = principal.getUsuario();
            return usuario.getRol().getNombreRol();
        }
        return "";
    }
}
