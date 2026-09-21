package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.cotizacion.dto.ServicioCotizacionRequest;
import com.lacasadelchef.erp.cotizacion.dto.ServicioCotizacionResponse;
import com.lacasadelchef.erp.entity.CotizacionVersion;
import com.lacasadelchef.erp.entity.ServicioCotizacion;
import com.lacasadelchef.erp.entity.TipoServicio;
import com.lacasadelchef.erp.repository.CotizacionVersionRepository;
import com.lacasadelchef.erp.repository.ServicioCotizacionRepository;
import com.lacasadelchef.erp.repository.TipoServicioRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioCotizacionServiceImpl implements ServicioCotizacionService {

    private static final String TABLA = "servicio_cotizacion";
    private static final String ESTADO_CREADA = "CREADA";

    private final ServicioCotizacionRepository servicioCotizacionRepository;
    private final CotizacionVersionRepository cotizacionVersionRepository;
    private final TipoServicioRepository tipoServicioRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<ServicioCotizacionResponse> listar(Integer idCotizacionVersion) {
        return servicioCotizacionRepository.findByCotizacionVersionIdCotizacionVersion(idCotizacionVersion).stream()
                .map(ServicioCotizacionResponse::desde)
                .toList();
    }

    @Override
    @Transactional
    public ServicioCotizacionResponse agregar(Integer idCotizacionVersion, ServicioCotizacionRequest request) {
        CotizacionVersion version = buscarVersionEditable(idCotizacionVersion);
        ServicioCotizacion servicio = new ServicioCotizacion();
        servicio.setCotizacionVersion(version);
        aplicar(request, servicio);
        servicio = servicioCotizacionRepository.save(servicio);

        entityManager.refresh(version);
        bitacoraMovimientoService.registrar(TABLA, servicio.getIdServicioCotizacion(), Operacion.INSERT);
        return ServicioCotizacionResponse.desde(servicio, version.getMontoTotal());
    }

    @Override
    @Transactional
    public ServicioCotizacionResponse actualizar(Integer idCotizacionVersion, Integer idServicio, ServicioCotizacionRequest request) {
        ServicioCotizacion servicio = buscarServicio(idCotizacionVersion, idServicio);
        validarEditable(servicio.getCotizacionVersion());
        aplicar(request, servicio);
        servicio = servicioCotizacionRepository.save(servicio);

        CotizacionVersion version = servicio.getCotizacionVersion();
        entityManager.refresh(version);
        return ServicioCotizacionResponse.desde(servicio, version.getMontoTotal());
    }

    @Override
    @Transactional
    public void eliminar(Integer idCotizacionVersion, Integer idServicio) {
        ServicioCotizacion servicio = buscarServicio(idCotizacionVersion, idServicio);
        validarEditable(servicio.getCotizacionVersion());
        servicioCotizacionRepository.delete(servicio);
        bitacoraMovimientoService.registrar(TABLA, idServicio, Operacion.DELETE);
    }

    private CotizacionVersion buscarVersionEditable(Integer idCotizacionVersion) {
        CotizacionVersion version = cotizacionVersionRepository.findById(idCotizacionVersion)
                .orElseThrow(() -> new ResourceNotFoundException("CotizacionVersion", idCotizacionVersion));
        validarEditable(version);
        return version;
    }

    private void validarEditable(CotizacionVersion version) {
        if (!ESTADO_CREADA.equalsIgnoreCase(version.getEstado().getNombre())) {
            throw new BusinessException(
                    "Solo se puede modificar los servicios de una version en estado CREADA (actual: %s)"
                            .formatted(version.getEstado().getNombre()));
        }
    }

    private ServicioCotizacion buscarServicio(Integer idCotizacionVersion, Integer idServicio) {
        ServicioCotizacion servicio = servicioCotizacionRepository.findById(idServicio)
                .orElseThrow(() -> new ResourceNotFoundException("ServicioCotizacion", idServicio));
        if (!servicio.getCotizacionVersion().getIdCotizacionVersion().equals(idCotizacionVersion)) {
            throw new ResourceNotFoundException("ServicioCotizacion", idServicio);
        }
        return servicio;
    }

    private void aplicar(ServicioCotizacionRequest request, ServicioCotizacion servicio) {
        TipoServicio tipoServicio = tipoServicioRepository.findById(request.idTipoServicio())
                .orElseThrow(() -> new ResourceNotFoundException("TipoServicio", request.idTipoServicio()));
        servicio.setTipoServicio(tipoServicio);
        servicio.setDescripcion(request.descripcion());
        servicio.setMonto(request.monto());
    }
}
