package com.lacasadelchef.erp.inventario;

import com.lacasadelchef.erp.inventario.dto.MovimientoInventarioRequest;
import com.lacasadelchef.erp.inventario.dto.MovimientoInventarioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movimientos-inventario")
@RequiredArgsConstructor
public class MovimientoInventarioController {

    private final MovimientoInventarioService movimientoInventarioService;

    @GetMapping
    public Page<MovimientoInventarioResponse> listar(@RequestParam(required = false) Integer idProducto,
                                                      @PageableDefault(size = 20, sort = "fechaMovimiento") Pageable pageable) {
        return movimientoInventarioService.listar(idProducto, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/movimientos-inventario', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public MovimientoInventarioResponse registrar(@Valid @RequestBody MovimientoInventarioRequest request) {
        return movimientoInventarioService.registrar(request);
    }
}
