package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Cotizacion;
import com.lacasadelchef.erp.entity.CotizacionVersion;
import com.lacasadelchef.erp.entity.DetalleCotizacion;
import com.lacasadelchef.erp.repository.CotizacionVersionRepository;
import com.lacasadelchef.erp.repository.DetalleCotizacionRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Arma el PDF de una version de cotizacion para entregarle al cliente. */
@Service
@RequiredArgsConstructor
public class CotizacionPdfService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color GRIS_ENCABEZADO = new Color(230, 230, 230);

    private final CotizacionVersionRepository cotizacionVersionRepository;
    private final DetalleCotizacionRepository detalleCotizacionRepository;

    @Transactional(readOnly = true)
    public byte[] generar(Integer idCotizacionVersion) {
        CotizacionVersion version = cotizacionVersionRepository.findById(idCotizacionVersion)
                .orElseThrow(() -> new ResourceNotFoundException("CotizacionVersion", idCotizacionVersion));
        List<DetalleCotizacion> detalles =
                detalleCotizacionRepository.findByCotizacionVersionIdCotizacionVersion(idCotizacionVersion);
        Cotizacion cotizacion = version.getCotizacion();

        Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font fuenteTexto = FontFactory.getFont(FontFactory.HELVETICA, 11);
        Font fuenteEncabezadoTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font fuenteCelda = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font fuenteTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            Document documento = new Document(PageSize.LETTER, 40, 40, 60, 40);
            PdfWriter.getInstance(documento, salida);
            documento.open();

            Paragraph titulo = new Paragraph("La Casa del Chef", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);

            Paragraph subtitulo = new Paragraph(
                    "Cotizacion #%d - Version %d".formatted(cotizacion.getIdCotizacion(), version.getNumeroVersion()),
                    fuenteTexto);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(20);
            documento.add(subtitulo);

            documento.add(new Paragraph("Cliente: " + cotizacion.getCliente().getNombre(), fuenteTexto));
            if (cotizacion.getCliente().getCorreo() != null) {
                documento.add(new Paragraph("Correo: " + cotizacion.getCliente().getCorreo(), fuenteTexto));
            }
            documento.add(new Paragraph(
                    "Fecha de cotizacion: " + cotizacion.getFechaCotizacion().format(FORMATO_FECHA), fuenteTexto));
            if (cotizacion.getFechaEvento() != null) {
                documento.add(new Paragraph(
                        "Fecha estimada del evento: " + cotizacion.getFechaEvento().format(FORMATO_FECHA), fuenteTexto));
            }
            Paragraph estado = new Paragraph("Estado: " + version.getEstado().getNombre(), fuenteTexto);
            estado.setSpacingAfter(15);
            documento.add(estado);

            PdfPTable tabla = new PdfPTable(new float[] {4f, 1.2f, 1.5f, 1.5f});
            tabla.setWidthPercentage(100);

            agregarCeldaEncabezado(tabla, "Menu", fuenteEncabezadoTabla);
            agregarCeldaEncabezado(tabla, "Cantidad", fuenteEncabezadoTabla);
            agregarCeldaEncabezado(tabla, "Precio unit.", fuenteEncabezadoTabla);
            agregarCeldaEncabezado(tabla, "Subtotal", fuenteEncabezadoTabla);

            for (DetalleCotizacion detalle : detalles) {
                tabla.addCell(new PdfPCell(new Phrase(detalle.getMenu().getNombreMenu(), fuenteCelda)));
                tabla.addCell(celdaAlineadaDerecha(String.valueOf(detalle.getCantidadPlatos()), fuenteCelda));
                tabla.addCell(celdaAlineadaDerecha(formatearMoneda(detalle.getPrecioUnitario()), fuenteCelda));
                tabla.addCell(celdaAlineadaDerecha(formatearMoneda(detalle.getSubtotal()), fuenteCelda));
            }

            documento.add(tabla);

            Paragraph total = new Paragraph("Total: " + formatearMoneda(version.getMontoTotal()), fuenteTotal);
            total.setAlignment(Element.ALIGN_RIGHT);
            total.setSpacingBefore(15);
            documento.add(total);

            documento.close();
            return salida.toByteArray();
        } catch (DocumentException ex) {
            throw new IllegalStateException("No se pudo generar el PDF de la cotizacion", ex);
        }
    }

    private void agregarCeldaEncabezado(PdfPTable tabla, String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBackgroundColor(GRIS_ENCABEZADO);
        tabla.addCell(celda);
    }

    private PdfPCell celdaAlineadaDerecha(String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return celda;
    }

    private String formatearMoneda(BigDecimal monto) {
        return monto == null ? "Q0.00" : "Q" + monto.setScale(2, RoundingMode.HALF_UP);
    }
}
