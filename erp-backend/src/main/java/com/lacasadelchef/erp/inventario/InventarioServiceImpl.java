package com.lacasadelchef.erp.inventario;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Inventario;
import com.lacasadelchef.erp.entity.Producto;
import com.lacasadelchef.erp.inventario.dto.InventarioMinimoRequest;
import com.lacasadelchef.erp.inventario.dto.InventarioResponse;
import com.lacasadelchef.erp.repository.InventarioRepository;
import com.lacasadelchef.erp.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private static final String TABLA = "inventario";

    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public Page<InventarioResponse> listar(Pageable pageable) {
        return inventarioRepository.findAll(pageable).map(InventarioResponse::desde);
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioResponse obtenerPorProducto(Integer idProducto) {
        return inventarioRepository.findByProductoIdProducto(idProducto)
                .map(InventarioResponse::desde)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El producto %d aun no tiene registro de inventario (sin movimientos ni minimo configurado)"
                                .formatted(idProducto)));
    }

    @Override
    @Transactional
    public InventarioResponse actualizarMinimo(Integer idProducto, InventarioMinimoRequest request) {
        Inventario inventario = inventarioRepository.findByProductoIdProducto(idProducto).orElseGet(() -> {
            Producto producto = productoRepository.findById(idProducto)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", idProducto));
            Inventario nuevo = new Inventario();
            nuevo.setProducto(producto);
            return nuevo;
        });
        boolean esNuevo = inventario.getIdInventario() == null;
        inventario.setCantidadMinima(request.cantidadMinima());
        inventario = inventarioRepository.save(inventario);
        if (esNuevo) {
            bitacoraMovimientoService.registrar(TABLA, inventario.getIdInventario(), Operacion.INSERT);
        }
        return InventarioResponse.desde(inventario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioResponse> alertasBajoStock() {
        return inventarioRepository.findBajoStockMinimo().stream()
                .map(InventarioResponse::desde)
                .toList();
    }
}
