package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.common.pdf.MembretePdf;
import com.lacasadelchef.erp.common.pdf.MontoEnLetras;
import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.Pago;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.repository.PagoRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReciboPagoPdfService {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale LOCALE_ES = Locale.of("es", "GT");
    private static final String ESTADO_ANULADO = "ANULADO";

    private static final Color VERDE = new Color(0x27, 0xae, 0x60);
    private static final Color GRIS_ENCABEZADO = new Color(0xe9, 0xed, 0xf1);
    private static final Color ROJO_ANULADO = new Color(0xc0, 0x39, 0x2b);
    private static final Color GRIS_ETIQUETA = new Color(0x6b, 0x77, 0x85);
    private static final Rectangle MEDIA_CARTA_HORIZONTAL = new Rectangle(612, 396);

    private final PagoRepository pagoRepository;

    public static String numeroRecibo(Integer idPago) {
        return "%06d".formatted(idPago);
    }

    @Transactional(readOnly = true)
    public byte[] generar(Integer idPago) {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", idPago));
        Evento evento = pago.getEvento();
        boolean anulado = ESTADO_ANULADO.equalsIgnoreCase(pago.getEstado().getNombre());

        Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15);
        Font fuenteEtiqueta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, GRIS_ETIQUETA);
        Font fuenteTexto = FontFactory.getFont(FontFactory.HELVETICA, 9.5f);
        Font fuenteNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f);
        Font fuenteMonto = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, Color.WHITE);
        Font fuenteNota = FontFactory.getFont(FontFactory.HELVETICA, 7.5f);

        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            Document documento = new Document(MEDIA_CARTA_HORIZONTAL, 30, 30, 72, 34);
            PdfWriter writer = PdfWriter.getInstance(documento, salida);
            writer.setPageEvent(anulado ? new MembreteAnulado() : new MembretePdf(true));
            documento.open();

            PdfPTable encabezado = tabla(new float[] {3f, 2f});
            encabezado.addCell(celda(new Phrase("RECIBO DE PAGO", fuenteTitulo), Element.ALIGN_LEFT));
            Phrase numero = new Phrase();
            numero.add(new Phrase("No. " + numeroRecibo(pago.getIdPago()) + "\n", fuenteNegrita));
            numero.add(new Phrase("Fecha: " + formatearFechaPago(pago.getFechaPago()), fuenteTexto));
            encabezado.addCell(celda(numero, Element.ALIGN_RIGHT));
            encabezado.setSpacingAfter(6);
            documento.add(encabezado);

            Cliente cliente = clienteDe(evento);
            PdfPTable quienCuanto = tabla(new float[] {1.6f, 1f});
            quienCuanto.addCell(celda(new Phrase("RECIBIMOS DE", fuenteEtiqueta), Element.ALIGN_LEFT));
            quienCuanto.addCell(celda(new Phrase("LA CANTIDAD DE", fuenteEtiqueta), Element.ALIGN_LEFT));
            Phrase datosCliente = new Phrase();
            datosCliente.add(new Phrase((cliente != null ? cliente.getNombre() : "Cliente no registrado") + "\n", fuenteNegrita));
            datosCliente.add(new Phrase("NIT: " + (cliente != null ? cliente.getNit() : "CF"), fuenteTexto));
            quienCuanto.addCell(celdaDosLineas(datosCliente));
            PdfPCell celdaMonto = celda(new Phrase(formatearMoneda(pago.getMonto()), fuenteMonto), Element.ALIGN_CENTER);
            celdaMonto.setBackgroundColor(VERDE);
            celdaMonto.setVerticalAlignment(Element.ALIGN_MIDDLE);
            celdaMonto.setPadding(6);
            quienCuanto.addCell(celdaMonto);
            documento.add(quienCuanto);

            PdfPTable letras = tabla(new float[] {1f});
            PdfPCell celdaLetras = celda(new Phrase("(" + MontoEnLetras.quetzales(pago.getMonto()) + ")", fuenteTexto),
                    Element.ALIGN_LEFT);
            celdaLetras.setBackgroundColor(GRIS_ENCABEZADO);
            celdaLetras.setPadding(5);
            letras.addCell(celdaLetras);
            letras.setSpacingBefore(4);
            letras.setSpacingAfter(6);
            documento.add(letras);

            PdfPTable conceptoForma = tabla(new float[] {1.6f, 1f});
            conceptoForma.addCell(celda(new Phrase("POR CONCEPTO DE", fuenteEtiqueta), Element.ALIGN_LEFT));
            conceptoForma.addCell(celda(new Phrase("FORMA DE PAGO", fuenteEtiqueta), Element.ALIGN_LEFT));
            conceptoForma.addCell(celdaDosLineas(new Phrase(concepto(pago, evento), fuenteTexto)));
            Phrase forma = new Phrase(pago.getMetodoPago().getNombreMetodo(), fuenteTexto);
            if (pago.getReferenciaTransaccion() != null && !pago.getReferenciaTransaccion().isBlank()) {
                forma.add(new Phrase("\nRef.: " + pago.getReferenciaTransaccion(), fuenteTexto));
            }
            conceptoForma.addCell(celdaDosLineas(forma));
            if (pago.getObservaciones() != null && !pago.getObservaciones().isBlank()) {
                PdfPCell observaciones = celda(new Phrase("Observaciones: " + pago.getObservaciones(), fuenteNota),
                        Element.ALIGN_LEFT);
                observaciones.setColspan(2);
                conceptoForma.addCell(observaciones);
            }
            conceptoForma.setSpacingAfter(6);
            documento.add(conceptoForma);

            if (pago.getCostoEvento() == null && !anulado) {
                BigDecimal total = totalDe(evento);
                BigDecimal abonado = pagoRepository.sumarAbonadoHasta(evento.getIdEvento(), pago.getIdPago());
                PdfPTable cuenta = tabla(new float[] {1f, 1f, 1f});
                PdfPCell titulo = celda(new Phrase("ESTADO DE CUENTA DEL EVENTO", fuenteEtiqueta), Element.ALIGN_LEFT);
                titulo.setColspan(3);
                cuenta.addCell(titulo);
                cuenta.addCell(dato("Total del evento", formatearMoneda(total), fuenteNota, fuenteNegrita));
                cuenta.addCell(dato("Abonado (incluye este pago)", formatearMoneda(abonado), fuenteNota, fuenteNegrita));
                cuenta.addCell(dato("Saldo pendiente", formatearMoneda(total.subtract(abonado).max(BigDecimal.ZERO)),
                        fuenteNota, fuenteNegrita));
                documento.add(cuenta);
            }

            PdfPTable firmas = tabla(new float[] {1f, 0.4f, 1f});
            firmas.addCell(firma("Recibido por: " + nombreDe(pago.getUsuario()), fuenteNota));
            firmas.addCell(celda(new Phrase(""), Element.ALIGN_LEFT));
            firmas.addCell(firma("Firma del cliente", fuenteNota));
            firmas.setSpacingBefore(26);
            firmas.setSpacingAfter(6);
            documento.add(firmas);

            Paragraph aviso = new Paragraph("Precios incluyen IVA · Recibo interno. No es un documento fiscal; "
                    + "la factura se emite por separado.", fuenteNota);
            aviso.setAlignment(Element.ALIGN_CENTER);
            documento.add(aviso);

            documento.close();
            return salida.toByteArray();
        } catch (DocumentException ex) {
            throw new IllegalStateException("No se pudo generar el recibo del pago", ex);
        }
    }

    private static Cliente clienteDe(Evento evento) {
        return evento.getCotizacionVersion() != null
                ? evento.getCotizacionVersion().getCotizacion().getCliente()
                : evento.getCliente();
    }

    private static BigDecimal totalDe(Evento evento) {
        BigDecimal total = evento.getCotizacionVersion() != null
                ? evento.getCotizacionVersion().getMontoTotal()
                : evento.getMontoMenu();
        return total == null ? BigDecimal.ZERO : total;
    }

    private static String concepto(Pago pago, Evento evento) {
        String descripcionEvento = "evento #%d · %s · %s".formatted(
                evento.getIdEvento(), evento.getTipoEvento().getNombreTipo(), formatearFecha(evento.getFechaEvento()));
        if (pago.getCostoEvento() != null) {
            return "Reembolso de costo extra (%s) del %s".formatted(
                    pago.getCostoEvento().getTipoCosto().getNombreTipo(), descripcionEvento);
        }
        return "Abono al " + descripcionEvento;
    }

    private static String nombreDe(Usuario usuario) {
        if (usuario == null) {
            return "";
        }
        return usuario.getEmpleado() != null ? usuario.getEmpleado().getNombreCompleto() : usuario.getUsername();
    }

    private static String formatearFecha(LocalDate fecha) {
        if (fecha == null) {
            return "fecha por definir";
        }
        return "%d de %s de %d".formatted(fecha.getDayOfMonth(),
                fecha.getMonth().getDisplayName(TextStyle.FULL, LOCALE_ES), fecha.getYear());
    }

    static String formatearFechaPago(LocalDateTime fechaPago) {
        return fechaPago.toLocalTime().equals(LocalTime.MIDNIGHT)
                ? fechaPago.format(FORMATO_FECHA)
                : fechaPago.format(FORMATO_FECHA_HORA);
    }

    private static String formatearMoneda(BigDecimal monto) {
        BigDecimal valor = (monto == null ? BigDecimal.ZERO : monto).setScale(2, RoundingMode.HALF_UP);
        return String.format(Locale.US, "Q%,.2f", valor);
    }

    private static PdfPTable tabla(float[] anchos) {
        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100);
        return tabla;
    }

    private static PdfPCell celda(Phrase contenido, int alineacion) {
        PdfPCell celda = new PdfPCell(contenido);
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(2);
        return celda;
    }

    private static PdfPCell celdaDosLineas(Phrase contenido) {
        PdfPCell celda = celda(contenido, Element.ALIGN_LEFT);
        celda.setLeading(0, 1.4f);
        return celda;
    }

    private static PdfPCell dato(String etiqueta, String valor, Font fuenteEtiqueta, Font fuenteValor) {
        Phrase contenido = new Phrase();
        contenido.add(new Phrase(etiqueta + "\n", fuenteEtiqueta));
        contenido.add(new Phrase(valor, fuenteValor));
        PdfPCell celda = celda(contenido, Element.ALIGN_LEFT);
        celda.setBackgroundColor(GRIS_ENCABEZADO);
        celda.setBorder(Rectangle.BOX);
        celda.setBorderColor(Color.WHITE);
        celda.setBorderWidth(2);
        celda.setPadding(5);
        return celda;
    }

    private static PdfPCell firma(String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBorder(Rectangle.TOP);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setPaddingTop(4);
        return celda;
    }

    private static final class MembreteAnulado extends MembretePdf {
        private final Font fuenteMarca = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 90, ROJO_ANULADO);

        MembreteAnulado() {
            super(true);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            super.onEndPage(writer, document);
            PdfContentByte cb = writer.getDirectContent();
            cb.saveState();
            PdfGState transparencia = new PdfGState();
            transparencia.setFillOpacity(0.25f);
            cb.setGState(transparencia);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase("ANULADO", fuenteMarca),
                    document.getPageSize().getWidth() / 2, document.getPageSize().getHeight() / 2, 45);
            cb.restoreState();
        }
    }
}
