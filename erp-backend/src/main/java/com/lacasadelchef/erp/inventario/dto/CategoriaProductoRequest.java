package com.lacasadelchef.erp.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoriaProductoRequest(

        @NotNull(message = "El tipo de inventario es obligatorio")
        Integer idTipoInventario,

        @NotBlank(message = "El nombre de la categoria es obligatorio")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombreCategoria
) {
}
