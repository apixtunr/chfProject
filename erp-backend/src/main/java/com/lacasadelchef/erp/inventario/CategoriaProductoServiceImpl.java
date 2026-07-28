package com.lacasadelchef.erp.inventario;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.CategoriaProducto;
import com.lacasadelchef.erp.entity.TipoInventario;
import com.lacasadelchef.erp.inventario.dto.CategoriaProductoRequest;
import com.lacasadelchef.erp.inventario.dto.CategoriaProductoResponse;
import com.lacasadelchef.erp.repository.CategoriaProductoRepository;
import com.lacasadelchef.erp.repository.TipoInventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaProductoServiceImpl implements CategoriaProductoService {

    private static final String TABLA = "categoria_producto";

    private final CategoriaProductoRepository categoriaProductoRepository;
    private final TipoInventarioRepository tipoInventarioRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaProductoResponse> listar() {
        return categoriaProductoRepository.findAll().stream()
                .map(CategoriaProductoResponse::desde)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaProductoResponse obtenerPorId(Integer id) {
        return CategoriaProductoResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public CategoriaProductoResponse crear(CategoriaProductoRequest request) {
        CategoriaProducto categoria = new CategoriaProducto();
        aplicar(request, categoria);
        categoria = categoriaProductoRepository.save(categoria);
        bitacoraMovimientoService.registrar(TABLA, categoria.getIdCategoria(), Operacion.INSERT);
        return CategoriaProductoResponse.desde(categoria);
    }

    @Override
    @Transactional
    public CategoriaProductoResponse actualizar(Integer id, CategoriaProductoRequest request) {
        CategoriaProducto categoria = buscar(id);
        aplicar(request, categoria);
        categoria = categoriaProductoRepository.save(categoria);
        bitacoraMovimientoService.registrar(TABLA, categoria.getIdCategoria(), Operacion.UPDATE);
        return CategoriaProductoResponse.desde(categoria);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        CategoriaProducto categoria = buscar(id);
        categoriaProductoRepository.delete(categoria);
        bitacoraMovimientoService.registrar(TABLA, categoria.getIdCategoria(), Operacion.DELETE);
    }

    private CategoriaProducto buscar(Integer id) {
        return categoriaProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CategoriaProducto", id));
    }

    private void aplicar(CategoriaProductoRequest request, CategoriaProducto categoria) {
        TipoInventario tipoInventario = tipoInventarioRepository.findById(request.idTipoInventario())
                .orElseThrow(() -> new ResourceNotFoundException("TipoInventario", request.idTipoInventario()));
        categoria.setTipoInventario(tipoInventario);
        categoria.setNombreCategoria(request.nombreCategoria().trim());
    }
}
