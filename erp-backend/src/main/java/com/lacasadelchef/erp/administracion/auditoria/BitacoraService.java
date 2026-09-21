package com.lacasadelchef.erp.administracion.auditoria;

import com.lacasadelchef.erp.administracion.auditoria.dto.BitacoraAccesoResponse;
import com.lacasadelchef.erp.administracion.auditoria.dto.BitacoraMovimientoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BitacoraService {

    Page<BitacoraMovimientoResponse> listarMovimientos(Integer idUsuario, String tabla,
                                                         LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable);

    Page<BitacoraAccesoResponse> listarAccesos(Integer idUsuario, String resultado,
                                                LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable);

    /** Tablas que aparecen en bitacora_movimiento, para poblar el filtro de la pantalla. */
    List<String> listarTablasConMovimientos();
}
