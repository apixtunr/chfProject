package com.lacasadelchef.erp.common.reporte;

import com.lacasadelchef.erp.common.reporte.ReporteTabla.Columna;
import com.lacasadelchef.erp.common.reporte.ReporteTabla.Fila;
import com.lacasadelchef.erp.common.reporte.ReporteTabla.Grupo;
import com.lacasadelchef.erp.common.reporte.ReporteTabla.Tipo;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Arma un {@link ReporteTabla} a partir de una lista de registros: cada reporte declara sus
 * columnas (como sacar cada valor del registro), la fecha por la que se agrupa y, si tiene,
 * agrupaciones propias (por persona, por producto...). El builder reparte las filas en
 * grupos, en orden cronologico, y calcula subtotales y totales de las columnas que suman.
 *
 * Agrupaciones de fecha: DIA, SEMANA (de lunes a domingo), MES y ANIO. NINGUNA (o null)
 * deja todo en un solo grupo.
 */
public class ReporteTablaBuilder<T> {

    private static final Locale ES = Locale.of("es", "GT");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private record Definicion<T>(Columna columna, Function<T, Object> valor) {
    }

    private final String titulo;
    private final List<T> registros;
    private final List<Definicion<T>> columnas = new ArrayList<>();
    private final Map<String, Function<T, String>> agrupacionesPropias = new HashMap<>();
    private Function<T, LocalDate> fecha = r -> null;
    private Predicate<T> excluir = r -> false;

    private ReporteTablaBuilder(String titulo, List<T> registros) {
        this.titulo = titulo;
        this.registros = registros;
    }

    public static <T> ReporteTablaBuilder<T> de(String titulo, List<T> registros) {
        return new ReporteTablaBuilder<>(titulo, registros);
    }

    public ReporteTablaBuilder<T> texto(String clave, String titulo, float ancho, Function<T, Object> valor) {
        return columna(new Columna(clave, titulo, Tipo.TEXTO, false, ancho), valor);
    }

    public ReporteTablaBuilder<T> fecha(String clave, String titulo, float ancho, Function<T, Object> valor) {
        return columna(new Columna(clave, titulo, Tipo.FECHA, false, ancho), valor);
    }

    public ReporteTablaBuilder<T> fechaHora(String clave, String titulo, float ancho, Function<T, Object> valor) {
        return columna(new Columna(clave, titulo, Tipo.FECHA_HORA, false, ancho), valor);
    }

    /** Numero entero o decimal; con sumar, lleva subtotal y total. */
    public ReporteTablaBuilder<T> numero(String clave, String titulo, float ancho, boolean sumar, Function<T, Object> valor) {
        return columna(new Columna(clave, titulo, Tipo.NUMERO, sumar, ancho), valor);
    }

    public ReporteTablaBuilder<T> monto(String clave, String titulo, float ancho, boolean sumar, Function<T, Object> valor) {
        return columna(new Columna(clave, titulo, Tipo.MONTO, sumar, ancho), valor);
    }

    private ReporteTablaBuilder<T> columna(Columna columna, Function<T, Object> valor) {
        columnas.add(new Definicion<>(columna, valor));
        return this;
    }

    /** La fecha del registro con la que se agrupa por dia, semana, mes o año. */
    public ReporteTablaBuilder<T> fechaDeAgrupacion(Function<T, LocalDate> fecha) {
        this.fecha = fecha;
        return this;
    }

    /** Agrupacion propia del reporte (por ejemplo "PERSONA"): la etiqueta de cada grupo. */
    public ReporteTablaBuilder<T> agrupacion(String clave, Function<T, String> etiqueta) {
        agrupacionesPropias.put(clave, etiqueta);
        return this;
    }

    /** Registros que se muestran pero no suman (recibos anulados). */
    public ReporteTablaBuilder<T> excluirDeTotales(Predicate<T> excluir) {
        this.excluir = excluir;
        return this;
    }

    public ReporteTabla construir(String agrupar) {
        Function<T, String> etiqueta = etiquetaDe(agrupar);
        Map<String, List<T>> porGrupo = new LinkedHashMap<>();
        // Las agrupaciones por fecha salen en orden cronologico aunque los registros no lo esten.
        List<T> ordenados = new ArrayList<>(registros);
        if (esDeFecha(agrupar)) {
            ordenados.sort((a, b) -> {
                LocalDate fa = fecha.apply(a);
                LocalDate fb = fecha.apply(b);
                if (fa == null || fb == null) {
                    return fa == null ? (fb == null ? 0 : 1) : -1;
                }
                return clavePeriodo(agrupar, fa).compareTo(clavePeriodo(agrupar, fb));
            });
        }
        for (T registro : ordenados) {
            porGrupo.computeIfAbsent(etiqueta.apply(registro), k -> new ArrayList<>()).add(registro);
        }

        List<Grupo> grupos = new ArrayList<>();
        Map<String, BigDecimal> totales = totalesEnCero();
        int cantidad = 0;
        int excluidas = 0;
        for (Map.Entry<String, List<T>> entrada : porGrupo.entrySet()) {
            Map<String, BigDecimal> subtotales = totalesEnCero();
            List<Fila> filas = new ArrayList<>();
            int cantidadGrupo = 0;
            for (T registro : entrada.getValue()) {
                boolean excluida = excluir.test(registro);
                Map<String, Object> valores = new LinkedHashMap<>();
                for (Definicion<T> d : columnas) {
                    Object valor = d.valor().apply(registro);
                    valores.put(d.columna().clave(), valor);
                    if (!excluida && d.columna().sumar() && valor != null) {
                        BigDecimal numero = new BigDecimal(valor.toString());
                        subtotales.merge(d.columna().clave(), numero, BigDecimal::add);
                        totales.merge(d.columna().clave(), numero, BigDecimal::add);
                    }
                }
                filas.add(new Fila(valores, excluida));
                if (excluida) {
                    excluidas++;
                } else {
                    cantidadGrupo++;
                }
            }
            cantidad += cantidadGrupo;
            grupos.add(new Grupo(entrada.getKey(), cantidadGrupo, filas, subtotales));
        }
        return new ReporteTabla(titulo, columnas.stream().map(Definicion::columna).toList(), grupos, totales,
                cantidad, excluidas);
    }

    private Map<String, BigDecimal> totalesEnCero() {
        Map<String, BigDecimal> mapa = new LinkedHashMap<>();
        columnas.stream().filter(d -> d.columna().sumar()).forEach(d -> mapa.put(d.columna().clave(), BigDecimal.ZERO));
        return mapa;
    }

    private static boolean esDeFecha(String agrupar) {
        return agrupar != null && List.of("DIA", "SEMANA", "MES", "ANIO").contains(agrupar);
    }

    private Function<T, String> etiquetaDe(String agrupar) {
        if (agrupar != null && agrupacionesPropias.containsKey(agrupar)) {
            Function<T, String> propia = agrupacionesPropias.get(agrupar);
            return r -> {
                String valor = propia.apply(r);
                return valor == null || valor.isBlank() ? "Sin especificar" : valor;
            };
        }
        if (!esDeFecha(agrupar)) {
            return r -> null;
        }
        return r -> {
            LocalDate f = fecha.apply(r);
            return f == null ? "Sin fecha" : etiquetaPeriodo(agrupar, f);
        };
    }

    /** Fecha que identifica al periodo, para ordenarlos: el dia, el lunes, el dia 1 del mes o del año. */
    static LocalDate clavePeriodo(String agrupar, LocalDate f) {
        return switch (agrupar) {
            case "SEMANA" -> f.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case "MES" -> f.withDayOfMonth(1);
            case "ANIO" -> f.withDayOfYear(1);
            default -> f;
        };
    }

    /** "Jueves 09/10/2026", "Semana del 05/10/2026 al 11/10/2026", "Octubre 2026" o "2026". */
    static String etiquetaPeriodo(String agrupar, LocalDate f) {
        return switch (agrupar) {
            case "DIA" -> mayuscula(f.getDayOfWeek().getDisplayName(TextStyle.FULL, ES)) + " " + f.format(FECHA);
            case "SEMANA" -> {
                LocalDate lunes = clavePeriodo("SEMANA", f);
                yield "Semana del %s al %s".formatted(lunes.format(FECHA), lunes.plusDays(6).format(FECHA));
            }
            case "MES" -> mayuscula(f.getMonth().getDisplayName(TextStyle.FULL, ES)) + " " + f.getYear();
            default -> String.valueOf(f.getYear());
        };
    }

    private static String mayuscula(String texto) {
        return texto.substring(0, 1).toUpperCase(ES) + texto.substring(1);
    }
}
