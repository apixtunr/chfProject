package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.cotizacion.dto.DetalleCotizacionRequest;
import com.lacasadelchef.erp.cotizacion.dto.DetalleCotizacionResponse;
import com.lacasadelchef.erp.entity.CotizacionVersion;
import com.lacasadelchef.erp.entity.DetalleCotizacion;
import com.lacasadelchef.erp.entity.Menu;
import com.lacasadelchef.erp.repository.CotizacionVersionRepository;
import com.lacasadelchef.erp.repository.DetalleCotizacionRepository;
import com.lacasadelchef.erp.repository.MenuRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DetalleCotizacionServiceImpl implements DetalleCotizacionService {

    private static final String TABLA = "detalle_cotizacion";
    private static final String ESTADO_CREADA = "CREADA";
    private static final String ESTADO_ACTIVO = "ACTIVO";

    private final DetalleCotizacionRepository detalleCotizacionRepository;
    private final CotizacionVersionRepository cotizacionVersionRepository;
    private final MenuRepository menuRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<DetalleCotizacionResponse> listar(Integer idCotizacionVersion) {
        return detalleCotizacionRepository.findByCotizacionVersionIdCotizacionVersion(idCotizacionVersion).stream()
                .map(DetalleCotizacionResponse::desde)
                .toList();
    }

    @Override
    @Transactional
    public DetalleCotizacionResponse agregar(Integer idCotizacionVersion, DetalleCotizacionRequest request) {
        CotizacionVersion version = buscarVersionEditable(idCotizacionVersion);
        Menu menu = buscarMenu(request.idMenu());

        DetalleCotizacion detalle = new DetalleCotizacion();
        detalle.setCotizacionVersion(version);
        detalle.setMenu(menu);
        aplicar(request, detalle);
        detalle = detalleCotizacionRepository.save(detalle);

        entityManager.refresh(version);
        bitacoraMovimientoService.registrar(TABLA, detalle.getIdDetalleCotizacion(), Operacion.INSERT);
        return DetalleCotizacionResponse.desde(detalle, version.getMontoTotal());
    }

    @Override
    @Transactional
    public DetalleCotizacionResponse actualizar(Integer idCotizacionVersion, Integer idDetalle, DetalleCotizacionRequest request) {
        DetalleCotizacion detalle = buscarDetalle(idCotizacionVersion, idDetalle);
        validarEditable(detalle.getCotizacionVersion());
        Menu menu = buscarMenu(request.idMenu());
        detalle.setMenu(menu);
        aplicar(request, detalle);
        detalle = detalleCotizacionRepository.save(detalle);

        CotizacionVersion version = detalle.getCotizacionVersion();
        entityManager.refresh(version);
        bitacoraMovimientoService.registrar(TABLA, detalle.getIdDetalleCotizacion(), Operacion.UPDATE);
        return DetalleCotizacionResponse.desde(detalle, version.getMontoTotal());
    }

    @Override
    @Transactional
    public void eliminar(Integer idCotizacionVersion, Integer idDetalle) {
        DetalleCotizacion detalle = buscarDetalle(idCotizacionVersion, idDetalle);
        validarEditable(detalle.getCotizacionVersion());
        detalleCotizacionRepository.delete(detalle);
        bitacoraMovimientoService.registrar(TABLA, idDetalle, Operacion.DELETE);
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
                    "Solo se puede modificar el detalle de una version en estado CREADA (actual: %s)"
                            .formatted(version.getEstado().getNombre()));
        }
    }

    private Menu buscarMenu(Integer idMenu) {
        Menu menu = menuRepository.findById(idMenu)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", idMenu));
        if (!ESTADO_ACTIVO.equalsIgnoreCase(menu.getEstado().getNombre())) {
            throw new BusinessException(
                    "El menu '%s' no esta activo y no se puede cotizar".formatted(menu.getNombreMenu()));
        }
        return menu;
    }

    private DetalleCotizacion buscarDetalle(Integer idCotizacionVersion, Integer idDetalle) {
        DetalleCotizacion detalle = detalleCotizacionRepository.findById(idDetalle)
                .orElseThrow(() -> new ResourceNotFoundException("DetalleCotizacion", idDetalle));
        if (!detalle.getCotizacionVersion().getIdCotizacionVersion().equals(idCotizacionVersion)) {
            throw new ResourceNotFoundException("DetalleCotizacion", idDetalle);
        }
        return detalle;
    }

    private void aplicar(DetalleCotizacionRequest request, DetalleCotizacion detalle) {
        detalle.setCantidadPlatos(request.cantidadPlatos());
        detalle.setPrecioUnitario(request.precioUnitario());
        detalle.setObservaciones(request.observaciones());
    }
}
