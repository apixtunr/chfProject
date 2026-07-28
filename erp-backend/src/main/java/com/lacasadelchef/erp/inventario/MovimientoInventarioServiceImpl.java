package com.lacasadelchef.erp.inventario;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.Inventario;
import com.lacasadelchef.erp.entity.MovimientoInventario;
import com.lacasadelchef.erp.entity.Producto;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.inventario.dto.MovimientoInventarioRequest;
import com.lacasadelchef.erp.inventario.dto.MovimientoInventarioResponse;
import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.InventarioRepository;
import com.lacasadelchef.erp.repository.MovimientoInventarioRepository;
import com.lacasadelchef.erp.repository.ProductoRepository;
import com.lacasadelchef.erp.security.UsuarioPrincipal;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MovimientoInventarioServiceImpl implements MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ProductoRepository productoRepository;
    private final EventoRepository eventoRepository;
    private final InventarioRepository inventarioRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoInventarioResponse> listar(Integer idProducto, Pageable pageable) {
        Page<MovimientoInventario> page = (idProducto == null)
                ? movimientoInventarioRepository.findAll(pageable)
                : movimientoInventarioRepository.findByProductoIdProducto(idProducto, pageable);
        return page.map(m -> MovimientoInventarioResponse.desde(m, stockActual(m.getProducto().getIdProducto())));
    }

    @Override
    @Transactional
    public MovimientoInventarioResponse registrar(MovimientoInventarioRequest request) {
        Producto producto = productoRepository.findById(request.idProducto())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", request.idProducto()));

        BigDecimal delta = calcularDelta(request.tipoMovimiento(), request.cantidad());
        BigDecimal stockActual = stockActual(request.idProducto());
        if (stockActual.add(delta).compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(
                    "El movimiento dejaria el stock de '%s' en negativo (actual: %s, solicitado: %s)"
                            .formatted(producto.getNombreProducto(), stockActual, request.cantidad()));
        }

        Evento evento = null;
        if (request.idEvento() != null) {
            evento = eventoRepository.findById(request.idEvento())
                    .orElseThrow(() -> new ResourceNotFoundException("Evento", request.idEvento()));
        }

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setEvento(evento);
        movimiento.setUsuario(usuarioActual());
        movimiento.setTipoMovimiento(request.tipoMovimiento());
        movimiento.setCantidad(request.cantidad());
        movimiento.setDescripcion(request.descripcion());
        movimiento = movimientoInventarioRepository.save(movimiento);

        // El trigger AFTER INSERT actualiza (o crea) la fila de inventario en otra tabla;
        // hay que refrescarla explicitamente para no devolver el stock desactualizado.
        Inventario inventario = inventarioRepository.findByProductoIdProducto(request.idProducto()).orElse(null);
        if (inventario != null) {
            entityManager.refresh(inventario);
        }
        BigDecimal cantidadTotalActual = inventario != null ? inventario.getCantidadTotal() : BigDecimal.ZERO;

        return MovimientoInventarioResponse.desde(movimiento, cantidadTotalActual);
    }

    private BigDecimal calcularDelta(String tipoMovimiento, BigDecimal cantidad) {
        TipoMovimiento tipo = TipoMovimiento.valueOf(tipoMovimiento);
        return switch (tipo) {
            case ENTRADA -> {
                validarPositiva(tipo, cantidad);
                yield cantidad;
            }
            case SALIDA -> {
                validarPositiva(tipo, cantidad);
                yield cantidad.negate();
            }
            case AJUSTE -> {
                if (cantidad.compareTo(BigDecimal.ZERO) == 0) {
                    throw new BusinessException("La cantidad de un ajuste no puede ser cero");
                }
                yield cantidad;
            }
        };
    }

    private void validarPositiva(TipoMovimiento tipo, BigDecimal cantidad) {
        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La cantidad debe ser mayor a 0 para movimientos de tipo %s".formatted(tipo));
        }
    }

    private BigDecimal stockActual(Integer idProducto) {
        return inventarioRepository.findByProductoIdProducto(idProducto)
                .map(Inventario::getCantidadTotal)
                .orElse(BigDecimal.ZERO);
    }

    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal principal) {
            return principal.getUsuario();
        }
        return null;
    }
}
