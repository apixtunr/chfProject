package com.lacasadelchef.erp.rentabilidad;

import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.rentabilidad.dto.RentabilidadEventoResponse;
import com.lacasadelchef.erp.rentabilidad.dto.RentabilidadResumenResponse;
import com.lacasadelchef.erp.rentabilidad.dto.RentabilidadResumenResponse.RentabilidadTipoResponse;
import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.EventoRepository.RentabilidadFila;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Rentabilidad de los eventos FINALIZADOS. Los creados, planificados o en curso no cuentan
 * (sus costos aun no estan completos y su ganancia saldria inflada) y los cancelados tampoco.
 */
@Service
@RequiredArgsConstructor
public class RentabilidadServiceImpl implements RentabilidadService {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final EventoRepository eventoRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RentabilidadEventoResponse> listarPorEvento(LocalDate fechaDesde, LocalDate fechaHasta,
                                                             Integer idCliente, Integer idTipoEvento, Pageable pageable) {
        List<RentabilidadEventoResponse> todos = calcular(null, fechaDesde, fechaHasta, idCliente, idTipoEvento);
        // Ya vienen ordenados del mas reciente al mas antiguo; son pocos, se paginan aqui.
        if (pageable.isUnpaged()) {
            return new PageImpl<>(todos);
        }
        int desde = (int) Math.min(pageable.getOffset(), todos.size());
        int hasta = Math.min(desde + pageable.getPageSize(), todos.size());
        return new PageImpl<>(todos.subList(desde, hasta), pageable, todos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public RentabilidadEventoResponse obtenerPorEvento(Integer idEvento) {
        return calcular(idEvento, null, null, null, null).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El evento %d no esta finalizado o no existe: su rentabilidad aun no se calcula".formatted(idEvento)));
    }

    @Override
    @Transactional(readOnly = true)
    public RentabilidadResumenResponse resumen(LocalDate fechaDesde, LocalDate fechaHasta,
                                               Integer idCliente, Integer idTipoEvento) {
        List<RentabilidadEventoResponse> eventos = calcular(null, fechaDesde, fechaHasta, idCliente, idTipoEvento);
        BigDecimal ingresos = sumar(eventos, RentabilidadEventoResponse::ingresosAcordados);
        BigDecimal ganancia = sumar(eventos, RentabilidadEventoResponse::gananciaAcordada);

        Map<String, List<RentabilidadEventoResponse>> porTipo = new LinkedHashMap<>();
        eventos.forEach(e -> porTipo.computeIfAbsent(e.tipoEventoNombre(), k -> new ArrayList<>()).add(e));
        List<RentabilidadTipoResponse> tipos = porTipo.entrySet().stream()
                .map(entrada -> {
                    BigDecimal ingresosTipo = sumar(entrada.getValue(), RentabilidadEventoResponse::ingresosAcordados);
                    BigDecimal gananciaTipo = sumar(entrada.getValue(), RentabilidadEventoResponse::gananciaAcordada);
                    return RentabilidadTipoResponse.builder()
                            .tipoEventoNombre(entrada.getKey())
                            .cantidadEventos(entrada.getValue().size())
                            .ingresosAcordados(ingresosTipo)
                            .gananciaAcordada(gananciaTipo)
                            .margen(porcentaje(gananciaTipo, ingresosTipo))
                            .build();
                })
                .sorted(Comparator.comparing(RentabilidadTipoResponse::gananciaAcordada).reversed())
                .toList();

        return RentabilidadResumenResponse.builder()
                .cantidadEventos(eventos.size())
                .ingresosAcordados(ingresos)
                .cobrado(sumar(eventos, RentabilidadEventoResponse::cobrado))
                .porCobrar(sumar(eventos, RentabilidadEventoResponse::porCobrar))
                .costoPersonal(sumar(eventos, RentabilidadEventoResponse::costoPersonal))
                .costoInventario(sumar(eventos, RentabilidadEventoResponse::costoInventario))
                .costoExtra(sumar(eventos, RentabilidadEventoResponse::costoExtra))
                .totalCostos(sumar(eventos, RentabilidadEventoResponse::totalCostos))
                .gananciaAcordada(ganancia)
                .gananciaCobrada(sumar(eventos, RentabilidadEventoResponse::gananciaCobrada))
                .margen(porcentaje(ganancia, ingresos))
                .porTipo(tipos)
                .build();
    }

    private List<RentabilidadEventoResponse> calcular(Integer idEvento, LocalDate fechaDesde, LocalDate fechaHasta,
                                                      Integer idCliente, Integer idTipoEvento) {
        return eventoRepository.rentabilidadFinalizados(idEvento, fechaDesde, fechaHasta, idCliente, idTipoEvento)
                .stream()
                .map(RentabilidadServiceImpl::desde)
                .toList();
    }

    /**
     * Las cuentas de un evento. Un costo extra que pago el cliente esta en los dos lados
     * (es costo y es ingreso), asi que no cambia la ganancia: solo la refleja completa.
     */
    static RentabilidadEventoResponse desde(RentabilidadFila fila) {
        BigDecimal precio = valor(fila.getPrecio());
        BigDecimal abonado = valor(fila.getAbonado());
        BigDecimal reembolsos = valor(fila.getReembolsos());
        BigDecimal personal = valor(fila.getCostoPersonal());
        BigDecimal inventario = valor(fila.getCostoInventario());
        BigDecimal extra = valor(fila.getCostoExtra());

        BigDecimal acordado = precio.add(reembolsos);
        BigDecimal cobrado = abonado.add(reembolsos);
        BigDecimal costos = personal.add(inventario).add(extra);
        BigDecimal gananciaAcordada = acordado.subtract(costos);
        return RentabilidadEventoResponse.builder()
                .idEvento(fila.getIdEvento())
                .fechaEvento(fila.getFechaEvento())
                .tipoEventoNombre(fila.getTipoEvento())
                .idCliente(fila.getIdCliente())
                .clienteNombre(fila.getClienteNombre())
                .ingresosAcordados(acordado)
                .cobrado(cobrado)
                .porCobrar(precio.subtract(abonado).max(BigDecimal.ZERO))
                .costoPersonal(personal)
                .costoInventario(inventario)
                .costoExtra(extra)
                .totalCostos(costos)
                .gananciaAcordada(gananciaAcordada)
                .gananciaCobrada(cobrado.subtract(costos))
                .margen(porcentaje(gananciaAcordada, acordado))
                .build();
    }

    private static BigDecimal porcentaje(BigDecimal parte, BigDecimal total) {
        if (total.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return parte.multiply(CIEN).divide(total, 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal valor(BigDecimal monto) {
        return (monto == null ? BigDecimal.ZERO : monto).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal sumar(List<RentabilidadEventoResponse> eventos,
                                    Function<RentabilidadEventoResponse, BigDecimal> campo) {
        return eventos.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
