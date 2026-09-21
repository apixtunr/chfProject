package com.lacasadelchef.erp.administracion.auditoria;

import com.lacasadelchef.erp.administracion.auditoria.dto.BitacoraAccesoResponse;
import com.lacasadelchef.erp.administracion.auditoria.dto.BitacoraMovimientoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bitacora")
@RequiredArgsConstructor
public class BitacoraController {

    private final BitacoraService bitacoraService;

    @GetMapping("/movimientos")
    public Page<BitacoraMovimientoResponse> listarMovimientos(
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(required = false) String tabla,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @PageableDefault(size = 20, sort = "fechaMovimiento", direction = Sort.Direction.DESC) Pageable pageable) {
        return bitacoraService.listarMovimientos(idUsuario, tabla, fechaDesde, fechaHasta, pageable);
    }

    @GetMapping("/movimientos/tablas")
    public List<String> listarTablasConMovimientos() {
        return bitacoraService.listarTablasConMovimientos();
    }

    @GetMapping("/accesos")
    public Page<BitacoraAccesoResponse> listarAccesos(
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(required = false) String resultado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @PageableDefault(size = 20, sort = "fechaAcceso", direction = Sort.Direction.DESC) Pageable pageable) {
        return bitacoraService.listarAccesos(idUsuario, resultado, fechaDesde, fechaHasta, pageable);
    }
}
