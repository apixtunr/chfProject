package com.lacasadelchef.erp.rentabilidad;

import com.lacasadelchef.erp.rentabilidad.dto.RentabilidadEventoResponse;
import com.lacasadelchef.erp.rentabilidad.dto.RentabilidadResumenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Reportes de solo lectura: ingresos, costos y ganancia por evento (vistas SQL ya calculadas). */
@RestController
@RequestMapping("/api/rentabilidad")
@RequiredArgsConstructor
public class RentabilidadController {

    private final RentabilidadService rentabilidadService;

    @GetMapping("/eventos")
    public Page<RentabilidadEventoResponse> listarPorEvento(@RequestParam(required = false) LocalDate fechaDesde,
                                                             @RequestParam(required = false) LocalDate fechaHasta,
                                                             @RequestParam(required = false) Integer idCliente,
                                                             @RequestParam(required = false) Integer idTipoEvento,
                                                             @PageableDefault(size = 20, sort = "fechaEvento") Pageable pageable) {
        return rentabilidadService.listarPorEvento(fechaDesde, fechaHasta, idCliente, idTipoEvento, pageable);
    }

    @GetMapping("/eventos/{idEvento}")
    public RentabilidadEventoResponse obtenerPorEvento(@PathVariable Integer idEvento) {
        return rentabilidadService.obtenerPorEvento(idEvento);
    }

    @GetMapping("/resumen")
    public RentabilidadResumenResponse resumen(@RequestParam(required = false) LocalDate fechaDesde,
                                               @RequestParam(required = false) LocalDate fechaHasta,
                                               @RequestParam(required = false) Integer idCliente,
                                               @RequestParam(required = false) Integer idTipoEvento) {
        return rentabilidadService.resumen(fechaDesde, fechaHasta, idCliente, idTipoEvento);
    }
}
