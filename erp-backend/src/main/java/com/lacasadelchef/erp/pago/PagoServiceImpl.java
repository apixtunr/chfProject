package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.CostoEvento;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.MetodoPago;
import com.lacasadelchef.erp.entity.Pago;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.pago.dto.PagoRequest;
import com.lacasadelchef.erp.pago.dto.PagoResponse;
import com.lacasadelchef.erp.repository.CostoEventoRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.MetodoPagoRepository;
import com.lacasadelchef.erp.repository.PagoRepository;
import com.lacasadelchef.erp.security.UsuarioPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private static final String TABLA = "pago";
    private static final String TIPO_ESTADO_PAGO = "PAGO";
    // Registrar un pago ya ES la confirmacion (no hay un paso de aprobacion aparte en este
    // negocio); nace CONFIRMADO. ANULADO sigue existiendo para cuando un pago valido se
    // invalida despues (cheque rebotado, transferencia reversada), sin borrar el registro.
    private static final String ESTADO_CONFIRMADO = "CONFIRMADO";

    private final PagoRepository pagoRepository;
    private final EventoRepository eventoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final EstadoRepository estadoRepository;
    private final CostoEventoRepository costoEventoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public Page<PagoResponse> listar(Integer idEvento, Pageable pageable) {
        Page<Pago> page = (idEvento == null)
                ? pagoRepository.findAll(pageable)
                : pagoRepository.findByEventoIdEvento(idEvento, pageable);
        return page.map(PagoResponse::desde);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponse obtenerPorId(Integer id) {
        return PagoResponse.desde(buscarPago(id));
    }

    @Override
    @Transactional
    public PagoResponse crear(PagoRequest request) {
        Usuario usuario = usuarioActual();
        if (usuario == null) {
            throw new BusinessException("No se pudo determinar el usuario autenticado");
        }
        Pago pago = new Pago();
        aplicar(request, pago);
        pago.setUsuario(usuario);
        pago.setEstado(buscarEstado(TIPO_ESTADO_PAGO, ESTADO_CONFIRMADO));
        pago = pagoRepository.save(pago);
        bitacoraMovimientoService.registrar(TABLA, pago.getIdPago(), Operacion.INSERT);
        return PagoResponse.desde(pago);
    }

    @Override
    @Transactional
    public PagoResponse actualizar(Integer id, PagoRequest request) {
        Pago pago = buscarPago(id);
        aplicar(request, pago);
        pago = pagoRepository.save(pago);
        bitacoraMovimientoService.registrar(TABLA, pago.getIdPago(), Operacion.UPDATE);
        return PagoResponse.desde(pago);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        // Si el pago tiene comprobantes asociados, la FK lo impide y el
        // GlobalExceptionHandler lo traduce a HTTP 409.
        Pago pago = buscarPago(id);
        pagoRepository.delete(pago);
        bitacoraMovimientoService.registrar(TABLA, pago.getIdPago(), Operacion.DELETE);
    }

    @Override
    @Transactional
    public PagoResponse cambiarEstado(Integer id, Integer idEstado) {
        Pago pago = buscarPago(id);
        Estado estado = estadoRepository.findById(idEstado)
                .orElseThrow(() -> new ResourceNotFoundException("Estado", idEstado));
        if (!TIPO_ESTADO_PAGO.equalsIgnoreCase(estado.getTipoEstado().getNombreTipo())) {
            throw new BusinessException(
                    "El estado %s no pertenece al catalogo de pagos".formatted(estado.getNombre()));
        }
        pago.setEstado(estado);
        pago = pagoRepository.save(pago);
        bitacoraMovimientoService.registrar(TABLA, pago.getIdPago(), Operacion.UPDATE);
        return PagoResponse.desde(pago);
    }

    private Pago buscarPago(Integer id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", id));
    }

    private Estado buscarEstado(String tipoEstado, String nombre) {
        return estadoRepository.findByTipoEstadoNombreTipoAndNombre(tipoEstado, nombre)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el estado %s/%s (revisar datos semilla)".formatted(tipoEstado, nombre)));
    }

    private void aplicar(PagoRequest request, Pago pago) {
        Evento evento = eventoRepository.findById(request.idEvento())
                .orElseThrow(() -> new ResourceNotFoundException("Evento", request.idEvento()));
        MetodoPago metodoPago = metodoPagoRepository.findById(request.idMetodoPago())
                .orElseThrow(() -> new ResourceNotFoundException("MetodoPago", request.idMetodoPago()));
        if (metodoPago.isRequiereReferencia()
                && (request.referenciaTransaccion() == null || request.referenciaTransaccion().isBlank())) {
            throw new BusinessException(
                    "El metodo de pago %s requiere una referencia de transaccion".formatted(metodoPago.getNombreMetodo()));
        }

        CostoEvento costoEvento = null;
        if (request.idCostoEvento() != null) {
            costoEvento = costoEventoRepository.findById(request.idCostoEvento())
                    .orElseThrow(() -> new ResourceNotFoundException("CostoEvento", request.idCostoEvento()));
        }

        pago.setEvento(evento);
        pago.setMetodoPago(metodoPago);
        pago.setCostoEvento(costoEvento);
        pago.setMonto(request.monto());
        pago.setReferenciaTransaccion(request.referenciaTransaccion());
        pago.setObservaciones(request.observaciones());
        if (request.fechaPago() != null) {
            pago.setFechaPago(request.fechaPago());
        }
    }

    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal principal) {
            return principal.getUsuario();
        }
        return null;
    }
}
