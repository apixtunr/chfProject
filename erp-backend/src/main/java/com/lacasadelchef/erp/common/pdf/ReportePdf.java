package com.lacasadelchef.erp.common.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Reporte interno en PDF (cuentas por cobrar, cobros, pago al personal, hoja de servicio):
 * el membrete de la empresa, un titulo con el periodo o los filtros, indicadores en
 * recuadros y tablas con encabezado de color. Cada reporte arma su contenido con estos
 * bloques, para que todos se vean iguales sin repetir el manejo de OpenPDF.
 */
public class ReportePdf {

    public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Color GRIS_FONDO = new Color(0xf1, 0xf3, 0xf6);
    private static final Color GRIS_FILA = new Color(0xf8, 0xf9, 0xfb);
    private static final Color GRIS_LINEA = new Color(0xdd, 0xe1, 0xe6);
    private static final Color GRIS_ETIQUETA = new Color(0x6b, 0x77, 0x85);

    private static final Font FUENTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15);
    private static final Font FUENTE_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 9, GRIS_ETIQUETA);
    private static final Font FUENTE_SECCION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.5f, MembretePdf.NAVY);
    private static final Font FUENTE_ETIQUETA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, GRIS_ETIQUETA);
    private static final Font FUENTE_VALOR = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FUENTE_DETALLE = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, GRIS_ETIQUETA);
    private static final Font FUENTE_ENCABEZADO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
    private static final Font FUENTE_CELDA = FontFactory.getFont(FontFactory.HELVETICA, 8.5f);
    private static final Font FUENTE_ATENUADA = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Font.STRIKETHRU,
            new Color(0x9c, 0xa3, 0xaf));
    private static final Font FUENTE_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f);
    private static final Font FUENTE_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 9.5f);

    /** Un recuadro de indicador: etiqueta arriba, valor grande y un detalle opcional debajo. */
    public record Indicador(String etiqueta, String valor, String detalle) {
    }

    /** Columna de una tabla: titulo, ancho relativo y si el contenido va a la derecha (montos). */
    public record Columna(String titulo, float ancho, boolean derecha) {

        public static Columna texto(String titulo, float ancho) {
            return new Columna(titulo, ancho, false);
        }

        public static Columna monto(String titulo, float ancho) {
            return new Columna(titulo, ancho, true);
        }
    }

    private final ByteArrayOutputStream salida = new ByteArrayOutputStream();
    private final Document documento;

    private ReportePdf(Rectangle tamano) {
        documento = new Document(tamano, 36, 36, 115, 60);
        try {
            PdfWriter writer = PdfWriter.getInstance(documento, salida);
            writer.setPageEvent(new MembreteInterno());
        } catch (DocumentException ex) {
            throw new IllegalStateException("No se pudo iniciar el reporte", ex);
        }
        documento.open();
    }

    /** Hoja carta vertical. */
    public static ReportePdf carta() {
        return new ReportePdf(PageSize.LETTER);
    }

    /** Hoja carta horizontal, para tablas con muchas columnas. */
    public static ReportePdf cartaHorizontal() {
        return new ReportePdf(PageSize.LETTER.rotate());
    }

    /** Titulo del reporte y, debajo, el periodo o los filtros aplicados y cuando se genero. */
    public ReportePdf titulo(String titulo, String subtitulo) {
        Paragraph p = new Paragraph(titulo, FUENTE_TITULO);
        p.setSpacingAfter(2);
        agregar(p);
        String generado = "Generado el " + LocalDateTime.now().format(FORMATO_FECHA_HORA);
        Paragraph sub = new Paragraph(subtitulo == null || subtitulo.isBlank()
                ? generado : subtitulo + "  ·  " + generado, FUENTE_SUBTITULO);
        sub.setSpacingAfter(10);
        agregar(sub);
        return this;
    }

    /** Fila de indicadores en recuadros grises, todos del mismo ancho. */
    public ReportePdf indicadores(List<Indicador> indicadores) {
        PdfPTable tabla = new PdfPTable(indicadores.size());
        tabla.setWidthPercentage(100);
        for (Indicador ind : indicadores) {
            Phrase contenido = new Phrase();
            contenido.add(new Phrase(ind.etiqueta().toUpperCase(Locale.ROOT) + "\n", FUENTE_ETIQUETA));
            contenido.add(new Phrase(ind.valor(), FUENTE_VALOR));
            if (ind.detalle() != null && !ind.detalle().isBlank()) {
                contenido.add(new Phrase("\n" + ind.detalle(), FUENTE_DETALLE));
            }
            PdfPCell celda = new PdfPCell(contenido);
            celda.setBackgroundColor(GRIS_FONDO);
            celda.setBorder(Rectangle.BOX);
            celda.setBorderColor(Color.WHITE);
            celda.setBorderWidth(3);
            celda.setPadding(7);
            celda.setLeading(0, 1.3f);
            tabla.addCell(celda);
        }
        tabla.setSpacingAfter(12);
        agregar(tabla);
        return this;
    }

    /** Titulo de una seccion, con una linea debajo. */
    public ReportePdf seccion(String titulo) {
        PdfPTable tabla = new PdfPTable(1);
        tabla.setWidthPercentage(100);
        PdfPCell celda = new PdfPCell(new Phrase(titulo, FUENTE_SECCION));
        celda.setBorder(Rectangle.BOTTOM);
        celda.setBorderColor(MembretePdf.NAVY);
        celda.setPaddingBottom(4);
        celda.setPaddingLeft(0);
        tabla.addCell(celda);
        tabla.setSpacingBefore(4);
        tabla.setSpacingAfter(6);
        tabla.setKeepTogether(true);
        agregar(tabla);
        return this;
    }

    /** Parrafo de texto normal. */
    public ReportePdf texto(String texto) {
        Paragraph p = new Paragraph(texto, FUENTE_TEXTO);
        p.setSpacingAfter(8);
        agregar(p);
        return this;
    }

    /** Pares etiqueta/valor en recuadros, en varias columnas (los datos de un evento, por ejemplo). */
    public ReportePdf datos(int columnas, List<String[]> pares) {
        PdfPTable tabla = new PdfPTable(columnas);
        tabla.setWidthPercentage(100);
        for (String[] par : pares) {
            Phrase contenido = new Phrase();
            contenido.add(new Phrase(par[0].toUpperCase(Locale.ROOT) + "\n", FUENTE_ETIQUETA));
            contenido.add(new Phrase(par[1], FUENTE_TOTAL));
            PdfPCell celda = new PdfPCell(contenido);
            celda.setBackgroundColor(GRIS_FONDO);
            celda.setBorder(Rectangle.BOX);
            celda.setBorderColor(Color.WHITE);
            celda.setBorderWidth(3);
            celda.setPadding(6);
            celda.setLeading(0, 1.35f);
            tabla.addCell(celda);
        }
        tabla.completeRow();
        tabla.setSpacingAfter(10);
        agregar(tabla);
        return this;
    }

    /**
     * Tabla con encabezado navy, filas alternadas y, si se indica, una fila de totales al pie.
     * Si no hay filas, muestra el texto de "sin datos" en una sola fila.
     */
    public ReportePdf tabla(List<Columna> columnas, List<List<String>> filas, List<String> totales, String sinDatos) {
        return tabla(columnas, filas, Set.of(), totales, sinDatos);
    }

    /** Igual que la otra, con filas atenuadas en gris (las que no suman, como un recibo anulado). */
    public ReportePdf tabla(List<Columna> columnas, List<List<String>> filas, Set<Integer> atenuadas,
                            List<String> totales, String sinDatos) {
        float[] anchos = new float[columnas.size()];
        for (int i = 0; i < columnas.size(); i++) {
            anchos[i] = columnas.get(i).ancho();
        }
        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        for (Columna c : columnas) {
            PdfPCell celda = new PdfPCell(new Phrase(c.titulo(), FUENTE_ENCABEZADO));
            celda.setBackgroundColor(MembretePdf.NAVY);
            celda.setBorderColor(MembretePdf.NAVY);
            celda.setHorizontalAlignment(c.derecha() ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
            celda.setPadding(5);
            tabla.addCell(celda);
        }
        if (filas.isEmpty()) {
            PdfPCell vacia = celdaCuerpo(sinDatos, false, false, FUENTE_DETALLE);
            vacia.setColspan(columnas.size());
            vacia.setHorizontalAlignment(Element.ALIGN_CENTER);
            vacia.setPadding(10);
            tabla.addCell(vacia);
        }
        for (int f = 0; f < filas.size(); f++) {
            List<String> fila = filas.get(f);
            for (int i = 0; i < columnas.size(); i++) {
                tabla.addCell(celdaCuerpo(fila.get(i), columnas.get(i).derecha(), f % 2 == 1,
                        atenuadas.contains(f) ? FUENTE_ATENUADA : FUENTE_CELDA));
            }
        }
        if (totales != null && !filas.isEmpty()) {
            for (int i = 0; i < columnas.size(); i++) {
                PdfPCell celda = celdaCuerpo(totales.get(i), columnas.get(i).derecha(), false, FUENTE_TOTAL);
                celda.setBackgroundColor(GRIS_FONDO);
                celda.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                celda.setBorderColor(MembretePdf.NAVY);
                tabla.addCell(celda);
            }
        }
        tabla.setSpacingAfter(12);
        agregar(tabla);
        return this;
    }

    public byte[] cerrar() {
        documento.close();
        return salida.toByteArray();
    }

    /** Respuesta HTTP para abrir el PDF en el navegador (o en el visor del sistema). */
    public static ResponseEntity<byte[]> respuesta(byte[] contenido, String nombreArchivo) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"%s.pdf\"".formatted(nombreArchivo))
                .cacheControl(CacheControl.noStore())
                .body(contenido);
    }

    // -------------------------------------------------------------------------------
    // Formatos que comparten todos los reportes
    // -------------------------------------------------------------------------------

    public static String moneda(BigDecimal monto) {
        BigDecimal valor = (monto == null ? BigDecimal.ZERO : monto).setScale(2, RoundingMode.HALF_UP);
        return String.format(Locale.US, "Q%,.2f", valor);
    }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "—" : fecha.format(FORMATO_FECHA);
    }

    /** "Del 01/10/2026 al 31/10/2026", "Desde el ...", "Hasta el ..." o "Todas las fechas". */
    public static String periodo(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null) {
            return "Del %s al %s".formatted(fecha(desde), fecha(hasta));
        }
        if (desde != null) {
            return "Desde el " + fecha(desde);
        }
        if (hasta != null) {
            return "Hasta el " + fecha(hasta);
        }
        return "Todas las fechas";
    }

    private static PdfPCell celdaCuerpo(String texto, boolean derecha, boolean alterna, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto == null ? "" : texto, fuente));
        celda.setBorder(Rectangle.BOTTOM);
        celda.setBorderColor(GRIS_LINEA);
        celda.setHorizontalAlignment(derecha ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setPadding(4.5f);
        if (alterna) {
            celda.setBackgroundColor(GRIS_FILA);
        }
        return celda;
    }

    private void agregar(Element elemento) {
        try {
            documento.add(elemento);
        } catch (DocumentException ex) {
            throw new IllegalStateException("No se pudo generar el reporte", ex);
        }
    }

    /** El membrete de la empresa; al pie, el numero de pagina en lugar del agradecimiento al cliente. */
    private static final class MembreteInterno extends MembretePdf {
        @Override
        protected String textoPie(PdfWriter writer) {
            return "La Casa del Chef  ·  Página " + writer.getPageNumber();
        }
    }
}
