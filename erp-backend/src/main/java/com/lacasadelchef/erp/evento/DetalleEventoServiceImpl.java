package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.evento.dto.DetalleEventoRequest;
import com.lacasadelchef.erp.evento.dto.DetalleEventoResponse;
import com.lacasadelchef.erp.entity.DetalleEvento;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.Menu;
import com.lacasadelchef.erp.repository.DetalleEventoRepository;
import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.MenuRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DetalleEventoServiceImpl implements DetalleEventoService {

    private static final String TABLA = "detalle_evento";
    private static final String ESTADO_EVENTO_PLANIFICADO = "PLANIFICADO";
    private static final String ESTADO_ACTIVO = "ACTIVO";

    private final DetalleEventoRepository detalleEventoRepository;
    private final EventoRepository eventoRepository;
    private final MenuRepository menuRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<DetalleEventoResponse> listar(Integer idEvento) {
        return detalleEventoRepository.findByEventoIdEvento(idEvento).stream()
                .map(DetalleEventoResponse::desde)
                .toList();
    }

    @Override
    @Transactional
    public DetalleEventoResponse agregar(Integer idEvento, DetalleEventoRequest request) {
        Evento evento = buscarEventoEditable(idEvento);
        Menu menu = buscarMenu(request.idMenu());

        DetalleEvento detalle = new DetalleEvento();
        detalle.setEvento(evento);
        detalle.setMenu(menu);
        aplicar(request, detalle);
        detalle = detalleEventoRepository.save(detalle);

        entityManager.refresh(evento);
        bitacoraMovimientoService.registrar(TABLA, detalle.getIdDetalleEvento(), Operacion.INSERT);
        return DetalleEventoResponse.desde(detalle, evento.getMontoMenu());
    }

    @Override
    @Transactional
    public DetalleEventoResponse actualizar(Integer idEvento, Integer idDetalle, DetalleEventoRequest request) {
        DetalleEvento detalle = buscarDetalle(idEvento, idDetalle);
        validarEditable(detalle.getEvento());
        Menu menu = buscarMenu(request.idMenu());
        detalle.setMenu(menu);
        aplicar(request, detalle);
        detalle = detalleEventoRepository.save(detalle);

        Evento evento = detalle.getEvento();
        entityManager.refresh(evento);
        bitacoraMovimientoService.registrar(TABLA, detalle.getIdDetalleEvento(), Operacion.UPDATE);
        return DetalleEventoResponse.desde(detalle, evento.getMontoMenu());
    }

    @Override
    @Transactional
    public void eliminar(Integer idEvento, Integer idDetalle) {
        DetalleEvento detalle = buscarDetalle(idEvento, idDetalle);
        validarEditable(detalle.getEvento());
        detalleEventoRepository.delete(detalle);
        bitacoraMovimientoService.registrar(TABLA, idDetalle, Operacion.DELETE);
    }

    private Evento buscarEventoEditable(Integer idEvento) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", idEvento));
        validarEditable(evento);
        return evento;
    }

    private void validarEditable(Evento evento) {
        if (evento.getCotizacionVersion() != null) {
            throw new BusinessException(
                    "Este evento nacio de una cotizacion; su menu se define en el detalle de esa cotizacion, no aqui");
        }
        if (!ESTADO_EVENTO_PLANIFICADO.equalsIgnoreCase(evento.getEstado().getNombre())) {
            throw new BusinessException(
                    "Solo se puede modificar el menu de un evento en estado PLANIFICADO (actual: %s)"
                            .formatted(evento.getEstado().getNombre()));
        }
    }

    private Menu buscarMenu(Integer idMenu) {
        Menu menu = menuRepository.findById(idMenu)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", idMenu));
        if (!ESTADO_ACTIVO.equalsIgnoreCase(menu.getEstado().getNombre())) {
            throw new BusinessException(
                    "El menu '%s' no esta activo".formatted(menu.getNombreMenu()));
        }
        return menu;
    }

    private DetalleEvento buscarDetalle(Integer idEvento, Integer idDetalle) {
        DetalleEvento detalle = detalleEventoRepository.findById(idDetalle)
                .orElseThrow(() -> new ResourceNotFoundException("DetalleEvento", idDetalle));
        if (!detalle.getEvento().getIdEvento().equals(idEvento)) {
            throw new ResourceNotFoundException("DetalleEvento", idDetalle);
        }
        return detalle;
    }

    private void aplicar(DetalleEventoRequest request, DetalleEvento detalle) {
        detalle.setCantidadPlatos(request.cantidadPlatos());
        detalle.setPrecioUnitario(request.precioUnitario());
        detalle.setObservaciones(request.observaciones());
    }
}
