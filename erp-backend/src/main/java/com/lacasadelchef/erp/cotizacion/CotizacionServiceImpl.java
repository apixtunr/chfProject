package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.cotizacion.dto.CotizacionRequest;
import com.lacasadelchef.erp.cotizacion.dto.CotizacionResponse;
import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.entity.Cotizacion;
import com.lacasadelchef.erp.entity.CotizacionVersion;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.TipoEvento;
import com.lacasadelchef.erp.entity.Ubicacion;
import com.lacasadelchef.erp.repository.ClienteRepository;
import com.lacasadelchef.erp.repository.CotizacionRepository;
import com.lacasadelchef.erp.repository.CotizacionVersionRepository;
import com.lacasadelchef.erp.repository.DetalleCotizacionRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.ServicioCotizacionRepository;
import com.lacasadelchef.erp.repository.TipoEventoRepository;
import com.lacasadelchef.erp.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CotizacionServiceImpl implements CotizacionService {

    private static final String TABLA = "cotizacion";
    private static final String TIPO_ESTADO_COTIZACION = "COTIZACION";
    private static final String ESTADO_CREADA = "CREADA";

    private final CotizacionRepository cotizacionRepository;
    private final CotizacionVersionRepository cotizacionVersionRepository;
    private final DetalleCotizacionRepository detalleCotizacionRepository;
    private final ServicioCotizacionRepository servicioCotizacionRepository;
    private final ClienteRepository clienteRepository;
    private final TipoEventoRepository tipoEventoRepository;
    private final UbicacionRepository ubicacionRepository;
    private final EstadoRepository estadoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public Page<CotizacionResponse> listar(Integer idCliente, Pageable pageable) {
        Page<Cotizacion> page = (idCliente == null)
                ? cotizacionRepository.findAll(pageable)
                : cotizacionRepository.findByClienteIdCliente(idCliente, pageable);
        return page.map(cotizacion -> CotizacionResponse.desde(cotizacion, buscarUltimaVersion(cotizacion.getIdCotizacion())));
    }

    @Override
    @Transactional(readOnly = true)
    public CotizacionResponse obtenerPorId(Integer id) {
        Cotizacion cotizacion = buscarCotizacion(id);
        return CotizacionResponse.desde(cotizacion, buscarUltimaVersion(id));
    }

    @Override
    @Transactional
    public CotizacionResponse crear(CotizacionRequest request) {
        Cotizacion cotizacion = new Cotizacion();
        aplicar(request, cotizacion);
        cotizacion = cotizacionRepository.save(cotizacion);
        CotizacionVersion version = crearVersionInicial(cotizacion);
        bitacoraMovimientoService.registrar(TABLA, cotizacion.getIdCotizacion(), Operacion.INSERT);
        return CotizacionResponse.desde(cotizacion, version);
    }

    @Override
    @Transactional
    public CotizacionResponse actualizar(Integer id, CotizacionRequest request) {
        Cotizacion cotizacion = buscarCotizacion(id);
        // Cliente, fecha, lugar y personas son parte de lo que se le envia al cliente: una
        // vez enviada, cambiarlos haria que el sistema ya no coincida con lo que el cliente
        // tiene en su PDF. Si hay que cambiarlos, se crea una version nueva.
        CotizacionVersion ultima = buscarUltimaVersion(id);
        if (ultima != null && !ESTADO_CREADA.equalsIgnoreCase(ultima.getEstado().getNombre())) {
            throw new BusinessException(
                    "La cotizacion ya fue enviada (version %d en %s): sus datos generales ya no se pueden cambiar"
                            .formatted(ultima.getNumeroVersion(), ultima.getEstado().getNombre()));
        }
        aplicar(request, cotizacion);
        cotizacion = cotizacionRepository.save(cotizacion);
        return CotizacionResponse.desde(cotizacion, ultima);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        // Solo se borra un borrador. Lo que ya se le envio al cliente es historial
        // comercial: queda como rechazado, no desaparece.
        Cotizacion cotizacion = buscarCotizacion(id);
        if (cotizacionVersionRepository.existsByCotizacionIdCotizacionAndEstadoNombreNot(id, ESTADO_CREADA)) {
            throw new BusinessException(
                    "Solo se puede eliminar una cotizacion que nunca se envio; esta ya tiene versiones enviadas");
        }
        // Las versiones (con sus platos y servicios) son parte del borrador: se van con el.
        // La base no borra en cascada, asi que sin esto la llave foranea lo impedia siempre.
        for (CotizacionVersion version : cotizacionVersionRepository.findByCotizacionIdCotizacionOrderByNumeroVersionDesc(id)) {
            Integer idVersion = version.getIdCotizacionVersion();
            detalleCotizacionRepository.deleteAll(detalleCotizacionRepository.findByCotizacionVersionIdCotizacionVersion(idVersion));
            servicioCotizacionRepository.deleteAll(servicioCotizacionRepository.findByCotizacionVersionIdCotizacionVersion(idVersion));
            cotizacionVersionRepository.delete(version);
        }
        cotizacionRepository.delete(cotizacion);
        bitacoraMovimientoService.registrar(TABLA, cotizacion.getIdCotizacion(), Operacion.DELETE);
    }

    /** Toda cotizacion nace con una version 1 en CREADA, lista para cargarle el detalle. */
    private CotizacionVersion crearVersionInicial(Cotizacion cotizacion) {
        Estado creada = estadoRepository.findByTipoEstadoNombreTipoAndNombre(TIPO_ESTADO_COTIZACION, ESTADO_CREADA)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el estado %s/%s (revisar datos semilla)".formatted(TIPO_ESTADO_COTIZACION, ESTADO_CREADA)));
        CotizacionVersion version = new CotizacionVersion();
        version.setCotizacion(cotizacion);
        version.setEstado(creada);
        version.setNumeroVersion(1);
        return cotizacionVersionRepository.save(version);
    }

    private CotizacionVersion buscarUltimaVersion(Integer idCotizacion) {
        List<CotizacionVersion> versiones =
                cotizacionVersionRepository.findByCotizacionIdCotizacionOrderByNumeroVersionDesc(idCotizacion);
        return versiones.isEmpty() ? null : versiones.get(0);
    }

    private Cotizacion buscarCotizacion(Integer id) {
        return cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotizacion", id));
    }

    private void aplicar(CotizacionRequest request, Cotizacion cotizacion) {
        Cliente cliente = clienteRepository.findById(request.idCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.idCliente()));
        // Un cliente inactivo no entra en cotizaciones nuevas; las que ya tenia no cambian
        // de cliente, asi que solo se revisa al asignarlo (alta o cambio de cliente).
        boolean clienteNuevo = cotizacion.getCliente() == null
                || !cotizacion.getCliente().getIdCliente().equals(cliente.getIdCliente());
        if (clienteNuevo && !cliente.estaActivo()) {
            throw new BusinessException("El cliente %s está inactivo: reactívelo antes de %s".formatted(cliente.getNombre(), "cotizarle"));
        }
        if (request.fechaEvento() != null && request.fechaEvento().isBefore(LocalDate.now())) {
            throw new BusinessException("La fecha del evento no puede estar en el pasado");
        }
        TipoEvento tipoEvento = tipoEventoRepository.findById(request.idTipoEvento())
                .orElseThrow(() -> new ResourceNotFoundException("TipoEvento", request.idTipoEvento()));
        Ubicacion ubicacion = ubicacionRepository.findById(request.idUbicacion())
                .orElseThrow(() -> new ResourceNotFoundException("Ubicacion", request.idUbicacion()));
        cotizacion.setCliente(cliente);
        cotizacion.setTipoEvento(tipoEvento);
        cotizacion.setUbicacion(ubicacion);
        cotizacion.setCantidadPersonas(request.cantidadPersonas());
        cotizacion.setFechaEvento(request.fechaEvento());
        cotizacion.setPresupuestoCliente(request.presupuestoCliente());
    }
}
