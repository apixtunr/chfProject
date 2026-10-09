package com.lacasadelchef.erp.common.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Membrete de los documentos que se le entregan al cliente (cotizacion, recibo de pago):
 * franja navy arriba con el logo del chef (el mismo del menu del sistema) y el nombre de
 * la empresa, y franja de agradecimiento abajo, en cada pagina.
 *
 * Hay dos tamanos: el normal, para la cotizacion en hoja carta (el documento deja margen
 * superior de 115 e inferior de 65), y el compacto, para el recibo en media carta (margen
 * superior de 72 e inferior de 34).
 */
public class MembretePdf extends PdfPageEventHelper {

    public static final Color NAVY = new Color(0x2c, 0x3e, 0x50);
    /** Contacto que se imprime en el encabezado de cada pagina. */
    public static final String TELEFONO_EMPRESA = "5173-0435";

    /** Se lee una sola vez: es el mismo en todas las paginas y todos los documentos. */
    private static final byte[] LOGO = leerLogo();

    private final float alturaHeader;
    private final float alturaFooter;
    private final float altoLogo;
    private final float[] posicionesTexto;
    private final Font fuenteTitulo;
    private final Font fuenteSubtitulo;
    private final Font fuenteContacto;
    private final Font fuentePie;

    /** Membrete normal, para hoja carta. */
    public MembretePdf() {
        this(false);
    }

    /** @param compacto franjas mas bajas, para documentos chicos como el recibo en media carta */
    public MembretePdf(boolean compacto) {
        alturaHeader = compacto ? 60f : 95f;
        alturaFooter = compacto ? 24f : 45f;
        altoLogo = compacto ? 50f : 80f;
        // Distancia desde el borde superior de la pagina a la linea de cada texto del encabezado.
        posicionesTexto = compacto ? new float[] {24, 39, 51} : new float[] {35, 55, 72};
        fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, compacto ? 16 : 22, Color.WHITE);
        fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, compacto ? 9 : 12, Color.WHITE);
        fuenteContacto = FontFactory.getFont(FontFactory.HELVETICA, compacto ? 7 : 9, Color.WHITE);
        fuentePie = FontFactory.getFont(FontFactory.HELVETICA, compacto ? 8 : 10, Color.WHITE);
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte cb = writer.getDirectContent();
        float anchoPagina = document.getPageSize().getWidth();
        float altoPagina = document.getPageSize().getHeight();

        cb.saveState();
        cb.setColorFill(NAVY);
        cb.rectangle(0, altoPagina - alturaHeader, anchoPagina, alturaHeader);
        cb.fill();
        cb.rectangle(0, 0, anchoPagina, alturaFooter);
        cb.fill();
        cb.restoreState();

        try {
            Image logo = Image.getInstance(LOGO);
            logo.scaleToFit(altoLogo, altoLogo);
            logo.setAbsolutePosition(document.leftMargin(),
                    altoPagina - alturaHeader + (alturaHeader - logo.getScaledHeight()) / 2);
            cb.addImage(logo);
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("No se pudo dibujar el logo en el PDF", e);
        }

        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                new Phrase("LA CASA DEL CHEF", fuenteTitulo), anchoPagina / 2, altoPagina - posicionesTexto[0], 0);
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                new Phrase("Servicios de Banquetes y Eventos", fuenteSubtitulo),
                anchoPagina / 2, altoPagina - posicionesTexto[1], 0);
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                new Phrase("Tel: " + TELEFONO_EMPRESA, fuenteContacto), anchoPagina / 2, altoPagina - posicionesTexto[2], 0);

        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                new Phrase("Gracias por su preferencia | La Casa del Chef", fuentePie),
                anchoPagina / 2, alturaFooter / 2f - 3, 0);
    }

    private static byte[] leerLogo() {
        try (InputStream entrada = MembretePdf.class.getResourceAsStream("/pdf/logo.png")) {
            if (entrada == null) {
                throw new IllegalStateException("Falta el logo /pdf/logo.png en los recursos");
            }
            return entrada.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
