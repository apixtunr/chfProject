package com.lacasadelchef.erp.common.reporte;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Un reporte en forma de tabla: sus columnas, las filas repartidas en grupos (por dia,
 * semana, mes, año o el criterio propio del reporte) con su subtotal, y el total general.
 * Es la misma estructura para todos los reportes por modulo, de modo que la pantalla, el
 * PDF y el CSV se arman igual para cualquiera de ellos.
 *
 * @param cantidad   filas que cuentan en los totales
 * @param excluidas  filas que se muestran pero no suman (por ejemplo, recibos anulados)
 */
public record ReporteTabla(
        String titulo,
        List<Columna> columnas,
        List<Grupo> grupos,
        Map<String, BigDecimal> totales,
        int cantidad,
        int excluidas
) {

    public enum Tipo { TEXTO, NUMERO, MONTO, FECHA, FECHA_HORA }

    /**
     * @param sumar si la columna lleva subtotal y total (montos, personas)
     * @param ancho ancho relativo de la columna en el PDF
     */
    public record Columna(String clave, String titulo, Tipo tipo, boolean sumar, float ancho) {
    }

    /** Un grupo de filas; sin agrupar hay un solo grupo, sin etiqueta. */
    public record Grupo(String etiqueta, int cantidad, List<Fila> filas, Map<String, BigDecimal> subtotales) {
    }

    /** @param excluida se muestra atenuada y no suma en subtotales ni totales */
    public record Fila(Map<String, Object> valores, boolean excluida) {
    }
}
