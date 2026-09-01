package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.cotizacion.dto.CotizacionResponse;
import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.entity.CotizacionVersion;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.TipoEvento;
import com.lacasadelchef.erp.entity.Ubicacion;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.evento.dto.ConteoResponse;
import com.lacasadelchef.erp.evento.dto.EventoRequest;
import com.lacasadelchef.erp.evento.dto.EventoResponse;
import com.lacasadelchef.erp.evento.dto.EventoResumenResponse;
import com.lacasadelchef.erp.repository.ClienteRepository;
import com.lacasadelchef.erp.repository.CotizacionVersionRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.TipoEventoRepository;
import com.lacasadelchef.erp.repository.UbicacionRepository;
import com.lacasadelchef.erp.security.UsuarioPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EventoServiceImpl implements EventoService {

    private static final String TABLA = "evento";
    private static final String TIPO_ESTADO_EVENTO = "EVENTO";
    private static final String ESTADO_PLANIFICADO = "PLANIFICADO";
    private static final String ESTADO_EN_CURSO = "EN CURSO";
    private static final String ESTADO_FINALIZADO = "FINALIZADO";
    private static final String ESTADO_CANCELADO = "CANCELADO";
    private static final String ESTADO_COTIZACION_ACEPTADA = "ACEPTADA";
    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    private static final Map<String, Set<String>> TRANSICIONES_VALIDAS = Map.of(
            ESTADO_PLANIFICADO, Set.of(ESTADO_EN_CURSO, ESTADO_CANCELADO),
            ESTADO_EN_CURSO, Set.of(ESTADO_FINALIZADO, ESTADO_CANCELADO));

    private final EventoRepository eventoRepository;
    private final CotizacionVersionRepository cotizacionVersionRepository;
    private final ClienteRepository clienteRepository;
    private final TipoEventoRepository tipoEventoRepository;
    private final UbicacionRepository ubicacionRepository;
    private final EstadoRepository estadoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

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
        evento.setEstado(buscarEstado(TIPO_ESTADO_EVENTO, ESTADO_PLANIFICADO));
        evento = eventoRepository.save(evento);
        bitacoraMovimientoService.registrar(TABLA, evento.getIdEvento(), Operacion.INSERT);
        return EventoResponse.desde(evento);
    }

    @Override
    @Transactional
    public EventoResponse actualizar(Integer id, EventoRequest request) {
        Evento evento = buscarEvento(id);
        aplicar(request, evento);
        evento = eventoRepository.save(evento);
        bitacoraMovimientoService.registrar(TABLA, evento.getIdEvento(), Operacion.UPDATE);
        return EventoResponse.desde(evento);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        // Si el evento tiene costos, pagos o asignaciones, la FK lo impide y el
        // GlobalExceptionHandler lo traduce a HTTP 409.
        Evento evento = buscarEvento(id);
        eventoRepository.delete(evento);
        bitacoraMovimientoService.registrar(TABLA, evento.getIdEvento(), Operacion.DELETE);
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
        if (ESTADO_CANCELADO.equals(estadoDestino) && !ROL_ADMINISTRADOR.equalsIgnoreCase(rolUsuarioActual())) {
            throw new BusinessException("Solo un administrador puede cancelar un evento");
        }

        evento.setEstado(estado);
        evento = eventoRepository.save(evento);
        bitacoraMovimientoService.registrar(TABLA, evento.getIdEvento(), Operacion.UPDATE);
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

        evento.setHoraInicio(request.horaInicio());
        evento.setHoraFin(request.horaFin());
        evento.setObservaciones(request.observaciones());
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
