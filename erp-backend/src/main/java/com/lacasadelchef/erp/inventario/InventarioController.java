package com.lacasadelchef.erp.inventario;

import com.lacasadelchef.erp.inventario.dto.InventarioMinimoRequest;
import com.lacasadelchef.erp.inventario.dto.InventarioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventarios")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping
    public Page<InventarioResponse> listar(@PageableDefault(size = 20) Pageable pageable) {
        return inventarioService.listar(pageable);
    }

    @GetMapping("/alertas")
    public List<InventarioResponse> alertasBajoStock() {
        return inventarioService.alertasBajoStock();
    }

    @GetMapping("/producto/{idProducto}")
    public InventarioResponse obtenerPorProducto(@PathVariable Integer idProducto) {
        return inventarioService.obtenerPorProducto(idProducto);
    }

    @PutMapping("/producto/{idProducto}/minimo")
    @PreAuthorize("@permisoService.tienePermiso('/api/inventarios', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public InventarioResponse actualizarMinimo(@PathVariable Integer idProducto,
                                               @Valid @RequestBody InventarioMinimoRequest request) {
        return inventarioService.actualizarMinimo(idProducto, request);
    }
}
