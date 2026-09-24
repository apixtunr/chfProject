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
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.TipoEventoRepository;
import com.lacasadelchef.erp.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CotizacionServiceImpl implements CotizacionService {

    private static final String TABLA = "cotizacion";
    private static final String TIPO_ESTADO_COTIZACION = "COTIZACION";
    private static final String ESTADO_CREADA = "CREADA";

    private final CotizacionRepository cotizacionRepository;
    private final CotizacionVersionRepository cotizacionVersionRepository;
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
        aplicar(request, cotizacion);
        cotizacion = cotizacionRepository.save(cotizacion);
        return CotizacionResponse.desde(cotizacion, buscarUltimaVersion(id));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        // Si la cotizacion tiene versiones con eventos ya generados, la FK lo impide
        // y el GlobalExceptionHandler lo traduce a HTTP 409.
        Cotizacion cotizacion = buscarCotizacion(id);
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
