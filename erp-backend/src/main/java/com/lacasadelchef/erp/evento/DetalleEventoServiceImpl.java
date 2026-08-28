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
import com.lacasadelchef.erp.entity.MenuPlato;
import com.lacasadelchef.erp.entity.id.MenuPlatoId;
import com.lacasadelchef.erp.repository.DetalleEventoRepository;
import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.MenuPlatoRepository;
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
    private final MenuPlatoRepository menuPlatoRepository;
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
        MenuPlato menuPlato = buscarMenuPlato(request.idMenu(), request.idPlato());

        DetalleEvento detalle = new DetalleEvento();
        detalle.setEvento(evento);
        aplicar(request, menuPlato, detalle);
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
        MenuPlato menuPlato = buscarMenuPlato(request.idMenu(), request.idPlato());
        aplicar(request, menuPlato, detalle);
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

    /** El precio nunca lo manda el cliente: se toma del menu_plato configurado en Administracion. */
    private MenuPlato buscarMenuPlato(Integer idMenu, Integer idPlato) {
        Menu menu = menuRepository.findById(idMenu)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", idMenu));
        if (!ESTADO_ACTIVO.equalsIgnoreCase(menu.getEstado().getNombre())) {
            throw new BusinessException(
                    "El menu '%s' no esta activo".formatted(menu.getNombreMenu()));
        }
        return menuPlatoRepository.findById(new MenuPlatoId(idMenu, idPlato))
                .orElseThrow(() -> new BusinessException(
                        "Ese plato no pertenece al menu '%s'".formatted(menu.getNombreMenu())));
    }

    private DetalleEvento buscarDetalle(Integer idEvento, Integer idDetalle) {
        DetalleEvento detalle = detalleEventoRepository.findById(idDetalle)
                .orElseThrow(() -> new ResourceNotFoundException("DetalleEvento", idDetalle));
        if (!detalle.getEvento().getIdEvento().equals(idEvento)) {
            throw new ResourceNotFoundException("DetalleEvento", idDetalle);
        }
        return detalle;
    }

    private void aplicar(DetalleEventoRequest request, MenuPlato menuPlato, DetalleEvento detalle) {
        detalle.setMenu(menuPlato.getMenu());
        detalle.setPlato(menuPlato.getPlato());
        detalle.setCantidadPlatos(request.cantidadPlatos());
        detalle.setPrecioUnitario(menuPlato.getPrecioUnitario());
        detalle.setObservaciones(request.observaciones());
    }
}
