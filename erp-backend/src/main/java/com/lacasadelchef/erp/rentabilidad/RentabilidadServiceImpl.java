package com.lacasadelchef.erp.rentabilidad;

import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.VRentabilidadEvento;
import com.lacasadelchef.erp.rentabilidad.dto.RentabilidadEventoResponse;
import com.lacasadelchef.erp.rentabilidad.dto.RentabilidadResumenResponse;
import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.VRentabilidadEventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentabilidadServiceImpl implements RentabilidadService {

    private final EventoRepository eventoRepository;
    private final VRentabilidadEventoRepository vRentabilidadEventoRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RentabilidadEventoResponse> listarPorEvento(LocalDate fechaDesde, LocalDate fechaHasta,
                                                             Integer idCliente, Integer idTipoEvento, Pageable pageable) {
        Page<Evento> eventos = eventoRepository.buscarPorFiltros(fechaDesde, fechaHasta, idCliente, idTipoEvento, pageable);
        Map<Integer, VRentabilidadEvento> vistasPorEvento = vistasPorEvento(eventos.getContent());
        return eventos.map(evento -> RentabilidadEventoResponse.desde(evento, vistasPorEvento.get(evento.getIdEvento())));
    }

    @Override
    @Transactional(readOnly = true)
    public RentabilidadEventoResponse obtenerPorEvento(Integer idEvento) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", idEvento));
        VRentabilidadEvento vista = vRentabilidadEventoRepository.findById(idEvento).orElse(null);
        return RentabilidadEventoResponse.desde(evento, vista);
    }

    @Override
    @Transactional(readOnly = true)
    public RentabilidadResumenResponse resumen(LocalDate fechaDesde, LocalDate fechaHasta,
                                               Integer idCliente, Integer idTipoEvento) {
        List<Evento> eventos = eventoRepository
                .buscarPorFiltros(fechaDesde, fechaHasta, idCliente, idTipoEvento, Pageable.unpaged())
                .getContent();
        List<VRentabilidadEvento> vistas = vRentabilidadEventoRepository.findAllById(
                eventos.stream().map(Evento::getIdEvento).toList());

        BigDecimal totalIngresos = sumar(vistas, VRentabilidadEvento::getTotalIngresos);
        BigDecimal totalCostos = sumar(vistas, VRentabilidadEvento::getTotalCostos);
        BigDecimal gananciaTotal = totalIngresos.subtract(totalCostos);
        BigDecimal margenPromedio = totalIngresos.compareTo(BigDecimal.ZERO) > 0
                ? gananciaTotal.divide(totalIngresos, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return RentabilidadResumenResponse.builder()
                .cantidadEventos(vistas.size())
                .totalIngresos(totalIngresos)
                .totalCostos(totalCostos)
                .gananciaTotal(gananciaTotal)
                .margenPromedio(margenPromedio)
                .build();
    }

    private Map<Integer, VRentabilidadEvento> vistasPorEvento(List<Evento> eventos) {
        List<Integer> ids = eventos.stream().map(Evento::getIdEvento).toList();
        return vRentabilidadEventoRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(VRentabilidadEvento::getIdEvento, Function.identity()));
    }

    private BigDecimal sumar(List<VRentabilidadEvento> vistas, Function<VRentabilidadEvento, BigDecimal> extractor) {
        return vistas.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
