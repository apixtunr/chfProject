package com.lacasadelchef.erp.common.reporte;

import com.lacasadelchef.erp.common.pdf.ReportePdf;
import com.lacasadelchef.erp.common.pdf.ReportePdf.Indicador;
import com.lacasadelchef.erp.common.reporte.ReporteTabla.Columna;
import com.lacasadelchef.erp.common.reporte.ReporteTabla.Fila;
import com.lacasadelchef.erp.common.reporte.ReporteTabla.Grupo;
import com.lacasadelchef.erp.common.reporte.ReporteTabla.Tipo;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * PDF de cualquier {@link ReporteTabla}: titulo con el periodo y los filtros, los totales
 * en recuadros y una tabla por grupo con su subtotal. Con mas de seis columnas la hoja va
 * horizontal para que quepan.
 */
public final class ReporteTablaPdf {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ReporteTablaPdf() {
    }

    /** Respuesta HTTP con el PDF, para el endpoint .../pdf de cada reporte. */
    public static ResponseEntity<byte[]> respuesta(ReporteTabla reporte, String subtitulo, String nombreArchivo) {
        return ReportePdf.respuesta(generar(reporte, subtitulo), nombreArchivo);
    }

    /** El periodo y, si los hay, los filtros aplicados (el texto de los filtros lo arma la pantalla). */
    public static String subtitulo(LocalDate desde, LocalDate hasta, String filtros) {
        String periodo = ReportePdf.periodo(desde, hasta);
        return filtros == null || filtros.isBlank() ? periodo : periodo + "  ·  " + filtros;
    }

    public static byte[] generar(ReporteTabla reporte, String subtitulo) {
        List<Columna> columnas = reporte.columnas();
        ReportePdf pdf = columnas.size() > 6 ? ReportePdf.cartaHorizontal() : ReportePdf.carta();
        pdf.titulo(reporte.titulo().toUpperCase(Locale.ROOT), subtitulo);

        List<Indicador> indicadores = new ArrayList<>();
        String detalle = reporte.excluidas() > 0
                ? reporte.excluidas() + (reporte.excluidas() == 1 ? " anulado, no suma" : " anulados, no suman")
                : "";
        indicadores.add(new Indicador("Registros", String.valueOf(reporte.cantidad()), detalle));
        for (Columna c : columnas) {
            if (c.sumar()) {
                indicadores.add(new Indicador("Total " + c.titulo().toLowerCase(Locale.ROOT),
                        formatear(c, reporte.totales().get(c.clave())), ""));
            }
        }
        pdf.indicadores(indicadores);

        List<ReportePdf.Columna> columnasPdf = columnas.stream()
                .map(c -> new ReportePdf.Columna(c.titulo(), c.ancho(), alineadaALaDerecha(c)))
                .toList();
        boolean variosGrupos = reporte.grupos().size() > 1 || reporte.grupos().stream().anyMatch(g -> g.etiqueta() != null);
        if (reporte.grupos().isEmpty()) {
            pdf.tabla(columnasPdf, List.of(), null, "No hay registros con estos filtros");
        }
        for (Grupo grupo : reporte.grupos()) {
            if (variosGrupos) {
                pdf.seccion("%s  ·  %d %s".formatted(grupo.etiqueta(), grupo.cantidad(),
                        grupo.cantidad() == 1 ? "registro" : "registros"));
            }
            List<List<String>> filas = new ArrayList<>();
            Set<Integer> atenuadas = new HashSet<>();
            for (int i = 0; i < grupo.filas().size(); i++) {
                Fila fila = grupo.filas().get(i);
                filas.add(columnas.stream().map(c -> formatear(c, fila.valores().get(c.clave()))).toList());
                if (fila.excluida()) {
                    atenuadas.add(i);
                }
            }
            pdf.tabla(columnasPdf, filas, atenuadas,
                    filaDeTotales(columnas, grupo.subtotales(), variosGrupos ? "Subtotal" : "Total"),
                    "No hay registros con estos filtros");
        }
        // Con varios grupos, el total general ya va en los recuadros de arriba.
        return pdf.cerrar();
    }

    private static List<String> filaDeTotales(List<Columna> columnas, Map<String, BigDecimal> totales, String rotulo) {
        if (columnas.stream().noneMatch(Columna::sumar)) {
            return null;
        }
        List<String> fila = new ArrayList<>();
        boolean rotuloPuesto = false;
        for (Columna c : columnas) {
            if (c.sumar()) {
                fila.add(formatear(c, totales.get(c.clave())));
            } else if (!rotuloPuesto) {
                fila.add(rotulo);
                rotuloPuesto = true;
            } else {
                fila.add("");
            }
        }
        return fila;
    }

    private static boolean alineadaALaDerecha(Columna c) {
        return c.tipo() == Tipo.MONTO || c.tipo() == Tipo.NUMERO;
    }

    static String formatear(Columna c, Object valor) {
        if (valor == null) {
            return "";
        }
        return switch (c.tipo()) {
            case MONTO -> ReportePdf.moneda(new BigDecimal(valor.toString()));
            case NUMERO -> new BigDecimal(valor.toString()).stripTrailingZeros().toPlainString();
            case FECHA -> valor instanceof LocalDate f ? ReportePdf.fecha(f) : valor.toString();
            case FECHA_HORA -> {
                if (valor instanceof LocalDateTime f) {
                    // Un pago registrado solo con el dia queda a las 00:00: se imprime sin hora.
                    yield f.toLocalTime().equals(LocalTime.MIDNIGHT) ? ReportePdf.fecha(f.toLocalDate()) : f.format(FECHA_HORA);
                }
                yield valor.toString();
            }
            default -> valor.toString();
        };
    }
}
