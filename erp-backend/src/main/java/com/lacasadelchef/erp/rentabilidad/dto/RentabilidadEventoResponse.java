package com.lacasadelchef.erp.rentabilidad.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Rentabilidad de un evento finalizado, con las dos lecturas de la ganancia:
 * - acordada: lo que se vendio menos lo que costo; dice si el precio y los costos estuvieron bien.
 * - cobrada: lo que realmente entro menos lo que costo; el dinero que quedo.
 * La diferencia entre las dos es lo que falta cobrar.
 */
@Builder
public record RentabilidadEventoResponse(
        Integer idEvento,
        LocalDate fechaEvento,
        String tipoEventoNombre,
        Integer idCliente,
        String clienteNombre,

        /** Precio del evento mas los costos extra que el cliente pago. */
        BigDecimal ingresosAcordados,
        /** Lo que entro: abonos al precio mas esos reembolsos (sin pagos anulados). */
        BigDecimal cobrado,
        /** Lo que el cliente todavia debe del precio del evento. */
        BigDecimal porCobrar,

        BigDecimal costoPersonal,
        BigDecimal costoInventario,
        BigDecimal costoExtra,
        BigDecimal totalCostos,

        BigDecimal gananciaAcordada,
        BigDecimal gananciaCobrada,
        /** Ganancia acordada sobre ingresos acordados, en porcentaje. */
        BigDecimal margen
) {
}
