package com.lacasadelchef.erp.inventario.dto;

import com.lacasadelchef.erp.entity.CategoriaProducto;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CategoriaProductoResponse(
        Integer idCategoria,
        Integer idTipoInventario,
        String tipoInventarioNombre,
        String nombreCategoria,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static CategoriaProductoResponse desde(CategoriaProducto categoria) {
        return CategoriaProductoResponse.builder()
                .idCategoria(categoria.getIdCategoria())
                .idTipoInventario(categoria.getTipoInventario().getIdTipoInventario())
                .tipoInventarioNombre(categoria.getTipoInventario().getNombreTipo())
                .nombreCategoria(categoria.getNombreCategoria())
                .fechaCreacion(categoria.getFechaCreacion())
                .fechaModificacion(categoria.getFechaModificacion())
                .build();
    }
}
