package com.lacasadelchef.erp.inicio;

import com.lacasadelchef.erp.entity.BitacoraMovimiento;
import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.entity.CotizacionVersion;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.Inventario;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.entity.VPagoEvento;
import com.lacasadelchef.erp.inicio.dto.ActividadPanelResponse;
import com.lacasadelchef.erp.inicio.dto.CobroPanelResponse;
import com.lacasadelchef.erp.inicio.dto.CotizacionPanelResponse;
import com.lacasadelchef.erp.inicio.dto.EventoPanelResponse;
import com.lacasadelchef.erp.inicio.dto.InsumoPanelResponse;
import com.lacasadelchef.erp.inicio.dto.PanelInicioResponse;
import com.lacasadelchef.erp.inicio.dto.ResumenInicioResponse;
import com.lacasadelchef.erp.inicio.dto.StockPanelResponse;
import com.lacasadelchef.erp.repository.RolOpcionRepository;
import com.lacasadelchef.erp.security.UsuarioPrincipal;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Arma el panel de inicio. Las consultas se hacen en bloque (una por tipo de dato para
 * todos los eventos a la vez) y no evento por evento, para que el inicio cargue con un
 * numero fijo de consultas sin importar cuantos eventos haya en la semana.
 *
 * Las secciones se filtran con la misma regla que el menu lateral: ADMINISTRADOR ve todo;
 * cualquier otro rol ve una seccion solo si tiene fila en rol_opcion para la pantalla de
 * donde salen esos datos (aunque sea "solo consulta").
 */
@Service
@RequiredArgsConstructor
public class InicioServiceImpl implements InicioService {

    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    private static final String URL_EVENTOS = "/api/eventos";
    private static final String URL_COTIZACIONES = "/api/cotizaciones";
    private static final String URL_INVENTARIOS = "/api/inventarios";
    private static final String URL_PAGOS = "/api/pagos";
    private static final String URL_BITACORA = "/api/bitacora";

    private static final String ESTADO_CREADO = "CREADO";
    private static final String ESTADO_PLANIFICADO = "PLANIFICADO";
    private static final String ESTADO_EN_CURSO = "EN CURSO";
    private static final String ESTADO_CANCELADO = "CANCELADO";

    /** Dias hacia adelante que cubre la agenda de la semana (hoy incluido). */
    private static final int DIAS_AGENDA = 7;
    /** Ventana para anticipar faltantes de insumos: lo que se va a consumir en estas dos semanas. */
    private static final int DIAS_INSUMOS = 14;
    private static final int LIMITE_LISTA = 8;

    private final EntityManager em;
    private final RolOpcionRepository rolOpcionRepository;

    @Override
    @Transactional(readOnly = true)
    public PanelInicioResponse panel() {
        Usuario usuario = usuarioActual();
        String rol = usuario != null ? usuario.getRol().getNombreRol() : "";
        LocalDate hoy = LocalDate.now();

        boolean verEventos = puedeVer(usuario, URL_EVENTOS);
        boolean verCotizaciones = puedeVer(usuario, URL_COTIZACIONES);
        boolean verInventario = puedeVer(usuario, URL_INVENTARIOS);
        boolean verPagos = puedeVer(usuario, URL_PAGOS);
        boolean verBitacora = puedeVer(usuario, URL_BITACORA);

        var panel = PanelInicioResponse.builder().fechaReferencia(hoy).rol(rol);
        var resumen = ResumenInicioResponse.builder();

        if (verEventos) {
            List<Evento> porPreparar = em.createQuery("""
                    SELECT e FROM Evento e
                    WHERE e.estado.nombre = :creado AND e.fechaEvento >= :hoy
                    ORDER BY e.fechaEvento, e.horaInicio
                    """, Evento.class)
                    .setParameter("creado", ESTADO_CREADO)
                    .setParameter("hoy", hoy)
                    .getResultList();

            List<Evento> agenda = em.createQuery("""
                    SELECT e FROM Evento e
                    WHERE e.estado.nombre IN :vigentes
                      AND e.fechaEvento BETWEEN :hoy AND :hasta
                    ORDER BY e.fechaEvento, e.horaInicio
                    """, Evento.class)
                    .setParameter("vigentes", List.of(ESTADO_CREADO, ESTADO_PLANIFICADO, ESTADO_EN_CURSO))
                    .setParameter("hoy", hoy)
                    .setParameter("hasta", hoy.plusDays(DIAS_AGENDA - 1L))
                    .getResultList();

            List<Evento> todos = new ArrayList<>(porPreparar);
            agenda.stream().filter(e -> !porPreparar.contains(e)).forEach(todos::add);
            Map<Integer, EventoPanelResponse> vistas = armarEventos(todos);

            panel.porPreparar(porPreparar.stream().limit(LIMITE_LISTA).map(e -> vistas.get(e.getIdEvento())).toList());
            panel.agenda(agenda.stream().map(e -> vistas.get(e.getIdEvento())).toList());
            resumen.eventosPorPreparar((long) porPreparar.size());
            resumen.eventosSemana((long) agenda.size());
        }

        if (verCotizaciones) {
            List<CotizacionVersion> enviadas = em.createQuery("""
                    SELECT cv FROM CotizacionVersion cv
                    WHERE cv.estado.nombre = 'ENVIADA'
                      AND cv.numeroVersion = (SELECT MAX(cv2.numeroVersion) FROM CotizacionVersion cv2
                                              WHERE cv2.cotizacion = cv.cotizacion)
                    ORDER BY cv.fechaVersion
                    """, CotizacionVersion.class)
                    .getResultList();
            List<CotizacionVersion> aceptadas = em.createQuery("""
                    SELECT cv FROM CotizacionVersion cv
                    WHERE cv.estado.nombre = 'ACEPTADA'
                      AND cv.cotizacion.fechaEvento >= :hoy
                      AND NOT EXISTS (SELECT 1 FROM Evento e WHERE e.cotizacionVersion = cv)
                    ORDER BY cv.cotizacion.fechaEvento
                    """, CotizacionVersion.class)
                    .setParameter("hoy", hoy)
                    .getResultList();

            panel.cotizacionesEnviadas(enviadas.stream().limit(LIMITE_LISTA).map(this::aCotizacion).toList());
            panel.cotizacionesAceptadasSinEvento(aceptadas.stream().limit(LIMITE_LISTA).map(this::aCotizacion).toList());
            resumen.cotizacionesEnviadas((long) enviadas.size());
        }

        if (verInventario) {
            List<Inventario> bajoStock = em.createQuery("""
                    SELECT i FROM Inventario i JOIN FETCH i.producto p
                    WHERE i.cantidadMinima > 0 AND i.cantidadTotal <= i.cantidadMinima
                    ORDER BY (i.cantidadTotal / i.cantidadMinima), p.nombreProducto
                    """, Inventario.class)
                    .getResultList();
            panel.bajoStock(bajoStock.stream().limit(LIMITE_LISTA).map(i -> new StockPanelResponse(
                    i.getProducto().getIdProducto(),
                    i.getProducto().getNombreProducto(),
                    i.getProducto().getUnidadMedida(),
                    i.getCantidadTotal(),
                    i.getCantidadMinima())).toList());
            panel.insumosFaltantes(insumosFaltantes(hoy));
            resumen.productosBajoStock((long) bajoStock.size());
        }

        if (verPagos) {
            List<VPagoEvento> conSaldo = em.createQuery("""
                    SELECT v FROM VPagoEvento v
                    WHERE v.pendiente > 0 AND v.estadoNombre <> :cancelado
                    ORDER BY v.fechaEvento
                    """, VPagoEvento.class)
                    .setParameter("cancelado", ESTADO_CANCELADO)
                    .getResultList();
            // Lo mas urgente primero: lo que ya paso o esta por pasar y sigue sin pagarse.
            panel.cobrosPendientes(conSaldo.stream()
                    .filter(v -> !v.getFechaEvento().isAfter(hoy.plusDays(30)))
                    .limit(LIMITE_LISTA)
                    .map(v -> new CobroPanelResponse(v.getIdEvento(), v.getFechaEvento(), v.getClienteNombre(),
                            v.getTipoEventoNombre(), v.getEstadoNombre(), v.getTotal(), v.getAbonado(), v.getPendiente()))
                    .toList());
            resumen.saldoPendiente(conSaldo.stream().map(VPagoEvento::getPendiente).reduce(BigDecimal.ZERO, BigDecimal::add));
            resumen.eventosConSaldo((long) conSaldo.size());
        }

        if (verBitacora) {
            panel.actividad(actividad(hoy));
        }

        return panel.resumen(resumen.build()).build();
    }

    // ------------------------------------------------------------------ eventos

    private Map<Integer, EventoPanelResponse> armarEventos(List<Evento> eventos) {
        Map<Integer, EventoPanelResponse> resultado = new HashMap<>();
        if (eventos.isEmpty()) {
            return resultado;
        }
        List<Integer> ids = eventos.stream().map(Evento::getIdEvento).toList();

        Set<Integer> conPersonal = idsCon("SELECT DISTINCT x.evento.idEvento FROM EventoEmpleado x WHERE x.evento.idEvento IN :ids", ids);
        Set<Integer> conVehiculo = idsCon("SELECT DISTINCT x.evento.idEvento FROM EventoVehiculo x WHERE x.evento.idEvento IN :ids", ids);
        Set<Integer> conInventario = idsCon("SELECT DISTINCT x.evento.idEvento FROM EventoInventario x WHERE x.evento.idEvento IN :ids", ids);
        Map<Integer, Set<String>> menusDirectos = menusPorEvento("""
                SELECT d.evento.idEvento, d.menu.nombreMenu FROM DetalleEvento d
                WHERE d.evento.idEvento IN :ids
                """, ids);

        List<Integer> idsVersion = eventos.stream()
                .filter(e -> e.getCotizacionVersion() != null)
                .map(e -> e.getCotizacionVersion().getIdCotizacionVersion())
                .toList();
        Map<Integer, Set<String>> menusCotizados = idsVersion.isEmpty() ? Map.of() : menusPorEvento("""
                SELECT d.cotizacionVersion.idCotizacionVersion, d.menu.nombreMenu FROM DetalleCotizacion d
                WHERE d.cotizacionVersion.idCotizacionVersion IN :ids
                """, idsVersion);

        for (Evento e : eventos) {
            Integer id = e.getIdEvento();
            boolean requiereMenu = e.getCotizacionVersion() == null;
            Set<String> menus = requiereMenu
                    ? menusDirectos.getOrDefault(id, Set.of())
                    : menusCotizados.getOrDefault(e.getCotizacionVersion().getIdCotizacionVersion(), Set.of());
            boolean tieneMenu = !requiereMenu || !menus.isEmpty();
            boolean tienePersonal = conPersonal.contains(id);
            boolean tieneVehiculos = conVehiculo.contains(id);
            boolean tieneInventario = conInventario.contains(id);

            // Mismo orden y mismas etiquetas que el mensaje de EventoServiceImpl.planificar().
            List<String> faltantes = new ArrayList<>();
            if (!tieneMenu) faltantes.add("Menú");
            if (!tienePersonal) faltantes.add("Personal");
            if (!tieneVehiculos) faltantes.add("Vehículos");
            if (!tieneInventario) faltantes.add("Inventario");

            Cliente cliente = e.getCotizacionVersion() != null
                    ? e.getCotizacionVersion().getCotizacion().getCliente()
                    : e.getCliente();

            resultado.put(id, EventoPanelResponse.builder()
                    .idEvento(id)
                    .fechaEvento(e.getFechaEvento())
                    .horaInicio(e.getHoraInicio())
                    .horaFin(e.getHoraFin())
                    .clienteNombre(cliente != null ? cliente.getNombre() : null)
                    .tipoEventoNombre(e.getTipoEvento().getNombreTipo())
                    .direccionUbicacion(e.getUbicacion().getDireccion())
                    .cantidadPersonas(e.getCantidadPersonas())
                    .estadoNombre(e.getEstado().getNombre())
                    .requiereMenu(requiereMenu)
                    .tieneMenu(tieneMenu)
                    .tienePersonal(tienePersonal)
                    .tieneVehiculos(tieneVehiculos)
                    .tieneInventario(tieneInventario)
                    .faltantes(faltantes)
                    .menus(List.copyOf(menus))
                    .build());
        }
        return resultado;
    }

    private Set<Integer> idsCon(String jpql, Collection<Integer> ids) {
        return new HashSet<>(em.createQuery(jpql, Integer.class).setParameter("ids", ids).getResultList());
    }

    private Map<Integer, Set<String>> menusPorEvento(String jpql, Collection<Integer> ids) {
        Map<Integer, Set<String>> mapa = new HashMap<>();
        for (Object[] fila : em.createQuery(jpql, Object[].class).setParameter("ids", ids).getResultList()) {
            mapa.computeIfAbsent((Integer) fila[0], k -> new LinkedHashSet<>()).add((String) fila[1]);
        }
        return mapa;
    }

    // ---------------------------------------------------------------- cotizacion

    private CotizacionPanelResponse aCotizacion(CotizacionVersion cv) {
        var c = cv.getCotizacion();
        return CotizacionPanelResponse.builder()
                .idCotizacion(c.getIdCotizacion())
                .idCotizacionVersion(cv.getIdCotizacionVersion())
                .numeroVersion(cv.getNumeroVersion())
                .clienteNombre(c.getCliente().getNombre())
                .tipoEventoNombre(c.getTipoEvento() != null ? c.getTipoEvento().getNombreTipo() : null)
                .fechaEvento(c.getFechaEvento())
                .montoTotal(cv.getMontoTotal())
                .fechaVersion(cv.getFechaVersion())
                .build();
    }

    // ---------------------------------------------------------------- inventario

    private List<InsumoPanelResponse> insumosFaltantes(LocalDate hoy) {
        List<Object[]> requeridos = em.createQuery("""
                SELECT p.idProducto, p.nombreProducto, p.unidadMedida,
                       SUM(ei.cantidad), MIN(e.fechaEvento), COUNT(DISTINCT e.idEvento)
                FROM EventoInventario ei
                JOIN ei.producto p
                JOIN ei.evento e
                WHERE ei.fechaConsumo IS NULL
                  AND e.estado.nombre IN :vigentes
                  AND e.fechaEvento BETWEEN :hoy AND :hasta
                GROUP BY p.idProducto, p.nombreProducto, p.unidadMedida
                """, Object[].class)
                .setParameter("vigentes", List.of(ESTADO_CREADO, ESTADO_PLANIFICADO))
                .setParameter("hoy", hoy)
                .setParameter("hasta", hoy.plusDays(DIAS_INSUMOS))
                .getResultList();
        if (requeridos.isEmpty()) {
            return List.of();
        }

        List<Integer> ids = requeridos.stream().map(f -> (Integer) f[0]).toList();
        Map<Integer, BigDecimal> stock = new HashMap<>();
        for (Object[] fila : em.createQuery("""
                SELECT i.producto.idProducto, i.cantidadTotal FROM Inventario i
                WHERE i.producto.idProducto IN :ids
                """, Object[].class).setParameter("ids", ids).getResultList()) {
            stock.put((Integer) fila[0], (BigDecimal) fila[1]);
        }

        List<InsumoPanelResponse> faltantes = new ArrayList<>();
        for (Object[] f : requeridos) {
            BigDecimal requerido = (BigDecimal) f[3];
            BigDecimal disponible = stock.getOrDefault((Integer) f[0], BigDecimal.ZERO);
            BigDecimal faltante = requerido.subtract(disponible);
            if (faltante.signum() > 0) {
                faltantes.add(new InsumoPanelResponse((Integer) f[0], (String) f[1], (String) f[2],
                        requerido, disponible, faltante, (LocalDate) f[4], (Long) f[5]));
            }
        }
        faltantes.sort((a, b) -> a.primerEvento().compareTo(b.primerEvento()));
        return faltantes.stream().limit(LIMITE_LISTA).toList();
    }

    // ----------------------------------------------------------------- bitacora

    private ActividadPanelResponse actividad(LocalDate hoy) {
        LocalDateTime inicioDia = hoy.atStartOfDay();
        Map<String, Long> accesos = new HashMap<>();
        for (Object[] fila : em.createQuery("""
                SELECT b.accion.nombre, COUNT(b) FROM BitacoraAcceso b
                WHERE b.fechaAcceso >= :desde
                GROUP BY b.accion.nombre
                """, Object[].class).setParameter("desde", inicioDia).getResultList()) {
            accesos.put((String) fila[0], (Long) fila[1]);
        }

        // La bitacora de movimientos guarda una fila por campo modificado: un solo "guardar"
        // puede dejar diez filas. Para contar y listar cambios se agrupan por registro.
        List<BitacoraMovimiento> ultimos = em.createQuery("""
                SELECT b FROM BitacoraMovimiento b LEFT JOIN FETCH b.usuario
                ORDER BY b.fechaMovimiento DESC
                """, BitacoraMovimiento.class)
                .setMaxResults(80)
                .getResultList();
        Map<String, ActividadPanelResponse.MovimientoReciente> recientes = new LinkedHashMap<>();
        for (BitacoraMovimiento b : ultimos) {
            String clave = b.getTablaAfectada() + "|" + b.getRegistroId() + "|" + b.getOperacion();
            recientes.putIfAbsent(clave, new ActividadPanelResponse.MovimientoReciente(
                    b.getUsuario() != null ? b.getUsuario().getUsername() : "sistema",
                    b.getTablaAfectada(),
                    b.getRegistroId(),
                    b.getOperacion(),
                    b.getFechaMovimiento()));
            if (recientes.size() == 6) {
                break;
            }
        }

        Long cambiosHoy = em.createQuery("""
                SELECT COUNT(DISTINCT CONCAT(b.tablaAfectada, '|', b.registroId, '|', b.operacion))
                FROM BitacoraMovimiento b
                WHERE b.fechaMovimiento >= :desde
                """, Long.class).setParameter("desde", inicioDia).getSingleResult();

        return new ActividadPanelResponse(
                accesos.getOrDefault("LOGIN", 0L),
                accesos.getOrDefault("LOGIN_FALLIDO", 0L),
                cambiosHoy != null ? cambiosHoy : 0L,
                List.copyOf(recientes.values()));
    }

    // ------------------------------------------------------------------ permisos

    private boolean puedeVer(Usuario usuario, String paginaUrl) {
        if (usuario == null) {
            return false;
        }
        if (ROL_ADMINISTRADOR.equalsIgnoreCase(usuario.getRol().getNombreRol())) {
            return true;
        }
        return rolOpcionRepository.findByRolIdRolAndOpcionPaginaUrl(usuario.getRol().getIdRol(), paginaUrl).isPresent();
    }

    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal principal) {
            return principal.getUsuario();
        }
        return null;
    }
}
