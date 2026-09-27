package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.cotizacion.dto.CotizacionVersionResponse;
import com.lacasadelchef.erp.entity.Cotizacion;
import com.lacasadelchef.erp.entity.CotizacionVersion;
import com.lacasadelchef.erp.entity.DetalleCotizacion;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.ServicioCotizacion;
import com.lacasadelchef.erp.repository.CotizacionRepository;
import com.lacasadelchef.erp.repository.CotizacionVersionRepository;
import com.lacasadelchef.erp.repository.DetalleCotizacionRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.ServicioCotizacionRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CotizacionVersionServiceImpl implements CotizacionVersionService {

    private static final String TIPO_ESTADO_COTIZACION = "COTIZACION";
    private static final String ESTADO_CREADA = "CREADA";
    private static final String ESTADO_ENVIADA = "ENVIADA";
    private static final String ESTADO_ACEPTADA = "ACEPTADA";
    private static final String ESTADO_RECHAZADA = "RECHAZADA";

    /**
     * Maquina de estados de una version de cotizacion. ACEPTADA y RECHAZADA son
     * terminales (no aparecen como llave): si el cliente pide mas cambios despues de
     * un rechazo, se crea una version nueva (siempre nace en CREADA) en vez de
     * reabrir la rechazada.
     */
    private static final Map<String, Set<String>> TRANSICIONES_VALIDAS = Map.of(
            ESTADO_CREADA, Set.of(ESTADO_ENVIADA),
            ESTADO_ENVIADA, Set.of(ESTADO_ACEPTADA, ESTADO_RECHAZADA));

    private final CotizacionVersionRepository cotizacionVersionRepository;
    private final CotizacionRepository cotizacionRepository;
    private final DetalleCotizacionRepository detalleCotizacionRepository;
    private final ServicioCotizacionRepository servicioCotizacionRepository;
    private final EstadoRepository estadoRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<CotizacionVersionResponse> listarPorCotizacion(Integer idCotizacion) {
        return cotizacionVersionRepository.findByCotizacionIdCotizacionOrderByNumeroVersionDesc(idCotizacion).stream()
                .map(CotizacionVersionResponse::desde)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CotizacionVersionResponse obtenerPorId(Integer idCotizacionVersion) {
        return CotizacionVersionResponse.desde(buscarVersion(idCotizacionVersion));
    }

    @Override
    @Transactional
    public CotizacionVersionResponse crearVersion(Integer idCotizacion, boolean copiarUltimoDetalle) {
        Cotizacion cotizacion = cotizacionRepository.findById(idCotizacion)
                .orElseThrow(() -> new ResourceNotFoundException("Cotizacion", idCotizacion));
        List<CotizacionVersion> versiones =
                cotizacionVersionRepository.findByCotizacionIdCotizacionOrderByNumeroVersionDesc(idCotizacion);
        if (versiones.isEmpty()) {
            throw new ResourceNotFoundException("La cotizacion %d no tiene versiones previas".formatted(idCotizacion));
        }
        CotizacionVersion ultima = versiones.get(0);
        if (!ESTADO_RECHAZADA.equalsIgnoreCase(ultima.getEstado().getNombre())) {
            throw new BusinessException(
                    "Solo se puede crear una nueva version cuando la ultima quedo en RECHAZADA (actual: %s)"
                            .formatted(ultima.getEstado().getNombre()));
        }

        Estado creada = estadoRepository.findByTipoEstadoNombreTipoAndNombre(TIPO_ESTADO_COTIZACION, ESTADO_CREADA)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el estado %s/%s (revisar datos semilla)".formatted(TIPO_ESTADO_COTIZACION, ESTADO_CREADA)));

        CotizacionVersion nueva = new CotizacionVersion();
        nueva.setCotizacion(cotizacion);
        nueva.setEstado(creada);
        nueva.setNumeroVersion(ultima.getNumeroVersion() + 1);
        nueva = cotizacionVersionRepository.save(nueva);

        if (copiarUltimoDetalle) {
            List<DetalleCotizacion> detallesAnteriores =
                    detalleCotizacionRepository.findByCotizacionVersionIdCotizacionVersion(ultima.getIdCotizacionVersion());
            for (DetalleCotizacion original : detallesAnteriores) {
                DetalleCotizacion copia = new DetalleCotizacion();
                copia.setCotizacionVersion(nueva);
                copia.setMenu(original.getMenu());
                copia.setPlato(original.getPlato());
                copia.setCantidadPlatos(original.getCantidadPlatos());
                copia.setPrecioUnitario(original.getPrecioUnitario());
                copia.setObservaciones(original.getObservaciones());
                copia.setBebida(original.getBebida());
                entityManager.persist(copia);
            }

            List<ServicioCotizacion> serviciosAnteriores =
                    servicioCotizacionRepository.findByCotizacionVersionIdCotizacionVersion(ultima.getIdCotizacionVersion());
            for (ServicioCotizacion original : serviciosAnteriores) {
                ServicioCotizacion copia = new ServicioCotizacion();
                copia.setCotizacionVersion(nueva);
                copia.setTipoServicio(original.getTipoServicio());
                copia.setDescripcion(original.getDescripcion());
                copia.setCantidad(original.getCantidad());
                copia.setMonto(original.getMonto());
                entityManager.persist(copia);
            }

            if (!detallesAnteriores.isEmpty() || !serviciosAnteriores.isEmpty()) {
                // El trigger de detalle_cotizacion/servicio_cotizacion actualiza monto_total de otra
                // fila (cotizacion_version); hay que refrescar para no devolver el valor en cero con
                // el que se creo "nueva".
                entityManager.flush();
                entityManager.refresh(nueva);
            }
        }

        return CotizacionVersionResponse.desde(nueva);
    }

    @Override
    @Transactional
    public CotizacionVersionResponse cambiarEstado(Integer idCotizacionVersion, Integer idEstado) {
        CotizacionVersion version = buscarVersion(idCotizacionVersion);
        Estado estado = estadoRepository.findById(idEstado)
                .orElseThrow(() -> new ResourceNotFoundException("Estado", idEstado));
        if (!TIPO_ESTADO_COTIZACION.equalsIgnoreCase(estado.getTipoEstado().getNombreTipo())) {
            throw new BusinessException(
                    "El estado %s no pertenece al catalogo de cotizaciones".formatted(estado.getNombre()));
        }

        String estadoActual = version.getEstado().getNombre().toUpperCase();
        String estadoDestino = estado.getNombre().toUpperCase();
        Set<String> permitidos = TRANSICIONES_VALIDAS.get(estadoActual);
        if (permitidos == null || !permitidos.contains(estadoDestino)) {
            throw new BusinessException(
                    "No se puede pasar la cotizacion de %s a %s. Si necesita mas cambios despues de un rechazo, cree una nueva version"
                            .formatted(estadoActual, estadoDestino));
        }

        if (ESTADO_ENVIADA.equals(estadoDestino)) {
            validarListaParaEnviar(version);
        }
        version.setEstado(estado);
        version = cotizacionVersionRepository.save(version);
        return CotizacionVersionResponse.desde(version);
    }

    /**
     * Lo que se le envia al cliente tiene que poder convertirse en un evento: al menos un
     * plato o servicio con un total mayor a cero, una fecha que todavia no paso, la hora
     * de inicio del servicio y la bebida de cada plato que la incluye. Se
     * revisa todo y se dice todo lo que falta de una vez, para no corregir de a uno.
     */
    private void validarListaParaEnviar(CotizacionVersion version) {
        Integer id = version.getIdCotizacionVersion();
        List<String> faltantes = new ArrayList<>();
        boolean tieneLineas = detalleCotizacionRepository.existsByCotizacionVersionIdCotizacionVersion(id)
                || servicioCotizacionRepository.existsByCotizacionVersionIdCotizacionVersion(id);
        if (!tieneLineas) {
            faltantes.add("agregar al menos un plato o servicio");
        } else if (version.getMontoTotal() == null || version.getMontoTotal().signum() <= 0) {
            faltantes.add("que el total sea mayor a Q 0.00");
        }
        LocalDate fechaEvento = version.getCotizacion().getFechaEvento();
        if (fechaEvento == null) {
            faltantes.add("indicar la fecha del evento");
        } else if (fechaEvento.isBefore(LocalDate.now())) {
            faltantes.add("actualizar la fecha del evento, que ya pasó (%s)".formatted(fechaEvento));
        }
        if (version.getCotizacion().getHoraInicio() == null) {
            faltantes.add("indicar la hora de inicio del servicio");
        }
        List<String> sinBebida = detalleCotizacionRepository.findByCotizacionVersionIdCotizacionVersion(id).stream()
                .filter(d -> d.getBebida() == null && !d.getPlato().opcionesBebida().isEmpty())
                .map(d -> d.getPlato().getNombrePlato())
                .distinct()
                .toList();
        if (!sinBebida.isEmpty()) {
            faltantes.add("elegir la bebida de " + String.join(", ", sinBebida));
        }
        if (!faltantes.isEmpty()) {
            throw new BusinessException("Para enviar la cotización falta " + enumerar(faltantes));
        }
    }

    /** "a", "a y b", "a, b y c". */
    private static String enumerar(List<String> partes) {
        if (partes.size() == 1) {
            return partes.get(0);
        }
        return String.join(", ", partes.subList(0, partes.size() - 1)) + " y " + partes.get(partes.size() - 1);
    }

    private CotizacionVersion buscarVersion(Integer id) {
        return cotizacionVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CotizacionVersion", id));
    }
}
