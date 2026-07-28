package com.lacasadelchef.erp.rentabilidad;

import com.lacasadelchef.erp.rentabilidad.dto.RentabilidadEventoResponse;
import com.lacasadelchef.erp.rentabilidad.dto.RentabilidadResumenResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface RentabilidadService {

    Page<RentabilidadEventoResponse> listarPorEvento(LocalDate fechaDesde, LocalDate fechaHasta,
                                                      Integer idCliente, Integer idTipoEvento, Pageable pageable);

    RentabilidadEventoResponse obtenerPorEvento(Integer idEvento);

    RentabilidadResumenResponse resumen(LocalDate fechaDesde, LocalDate fechaHasta,
                                        Integer idCliente, Integer idTipoEvento);
}
