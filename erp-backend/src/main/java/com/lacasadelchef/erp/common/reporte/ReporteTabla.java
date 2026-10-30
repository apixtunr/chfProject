package com.lacasadelchef.erp.common.reporte;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ReporteTabla(
        String titulo,
        List<Columna> columnas,
        List<Grupo> grupos,
        Map<String, BigDecimal> totales,
        int cantidad,
        int excluidas
) {

    public enum Tipo { TEXTO, NUMERO, MONTO, FECHA, FECHA_HORA }

    public record Columna(String clave, String titulo, Tipo tipo, boolean sumar, float ancho) {
    }

    public record Grupo(String etiqueta, int cantidad, List<Fila> filas, Map<String, BigDecimal> subtotales) {
    }

    public record Fila(Map<String, Object> valores, boolean excluida) {
    }
}
