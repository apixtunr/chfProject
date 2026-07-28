package com.lacasadelchef.erp.inventario;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.CategoriaProducto;
import com.lacasadelchef.erp.entity.Producto;
import com.lacasadelchef.erp.inventario.dto.ProductoRequest;
import com.lacasadelchef.erp.inventario.dto.ProductoResponse;
import com.lacasadelchef.erp.repository.CategoriaProductoRepository;
import com.lacasadelchef.erp.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private static final String TABLA = "producto";

    private final ProductoRepository productoRepository;
    private final CategoriaProductoRepository categoriaProductoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> listar(String nombre, Pageable pageable) {
        Page<Producto> page = (nombre == null || nombre.isBlank())
                ? productoRepository.findAll(pageable)
                : productoRepository.findByNombreProductoContainingIgnoreCase(nombre.trim(), pageable);
        return page.map(ProductoResponse::desde);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Integer id) {
        return ProductoResponse.desde(buscarProducto(id));
    }

    @Override
    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Producto producto = new Producto();
        aplicar(request, producto);
        producto = productoRepository.save(producto);
        bitacoraMovimientoService.registrar(TABLA, producto.getIdProducto(), Operacion.INSERT);
        return ProductoResponse.desde(producto);
    }

    @Override
    @Transactional
    public ProductoResponse actualizar(Integer id, ProductoRequest request) {
        Producto producto = buscarProducto(id);
        aplicar(request, producto);
        producto = productoRepository.save(producto);
        bitacoraMovimientoService.registrar(TABLA, producto.getIdProducto(), Operacion.UPDATE);
        return ProductoResponse.desde(producto);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        // Si el producto tiene inventario o movimientos asociados, la FK lo impide
        // y el GlobalExceptionHandler lo traduce a HTTP 409.
        Producto producto = buscarProducto(id);
        productoRepository.delete(producto);
        bitacoraMovimientoService.registrar(TABLA, producto.getIdProducto(), Operacion.DELETE);
    }

    private Producto buscarProducto(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
    }

    private void aplicar(ProductoRequest request, Producto producto) {
        CategoriaProducto categoria = categoriaProductoRepository.findById(request.idCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("CategoriaProducto", request.idCategoria()));
        producto.setCategoria(categoria);
        producto.setNombreProducto(request.nombreProducto().trim());
        producto.setUnidadMedida(request.unidadMedida().trim());
        producto.setPrecioUnitario(request.precioUnitario());
    }
}
