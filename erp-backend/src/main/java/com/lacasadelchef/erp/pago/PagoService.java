package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.pago.dto.EventoPagoResponse;
import com.lacasadelchef.erp.pago.dto.PagoRequest;
import com.lacasadelchef.erp.pago.dto.PagoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface PagoService {

    Page<PagoResponse> listar(Integer idEvento, Pageable pageable);

    /** Eventos con su saldo (total/abonado/pendiente), para la pantalla principal de Pagos.
     * filtro: PENDIENTE (default) o PAGADO. */
    Page<EventoPagoResponse> listarEventosConSaldo(String filtro, Integer idCliente,
                                                    LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable);

    PagoResponse obtenerPorId(Integer id);

    PagoResponse crear(PagoRequest request);

    PagoResponse actualizar(Integer id, PagoRequest request);

    void eliminar(Integer id);

    PagoResponse cambiarEstado(Integer id, Integer idEstado);
}
