package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.entity.CotizacionVersion;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.repository.CotizacionVersionRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Pasa a VENCIDA las cotizaciones ENVIADAS cuya vigencia ya termino (vigente_hasta es el
 * ultimo dia valido). Revisa al arrancar y cada hora: basta con una vez al dia, pero asi
 * no depende de que el servidor este encendido a una hora fija.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CotizacionVencimientoJob {

    private static final String TIPO_ESTADO_COTIZACION = "COTIZACION";
    private static final String ESTADO_ENVIADA = "ENVIADA";

    private final CotizacionVersionRepository cotizacionVersionRepository;
    private final EstadoRepository estadoRepository;

    @Scheduled(initialDelay = 60 * 1000, fixedRate = 60 * 60 * 1000)
    @Transactional
    public void vencerCotizaciones() {
        List<CotizacionVersion> vencidas =
                cotizacionVersionRepository.findByEstadoNombreAndVigenteHastaBefore(ESTADO_ENVIADA, LocalDate.now());
        if (vencidas.isEmpty()) {
            return;
        }
        Estado vencida = estadoRepository
                .findByTipoEstadoNombreTipoAndNombre(TIPO_ESTADO_COTIZACION, CotizacionVersionServiceImpl.ESTADO_VENCIDA)
                .orElseThrow(() -> new IllegalStateException("No existe el estado COTIZACION/VENCIDA (revisar V27)"));
        vencidas.forEach(version -> version.setEstado(vencida));
        cotizacionVersionRepository.saveAll(vencidas);
        log.info("Vencimiento de cotizaciones: {} pasadas a VENCIDA", vencidas.size());
    }
}
