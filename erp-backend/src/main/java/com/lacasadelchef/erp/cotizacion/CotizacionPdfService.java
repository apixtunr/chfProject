package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.entity.Cotizacion;
import com.lacasadelchef.erp.entity.CotizacionVersion;
import com.lacasadelchef.erp.entity.DetalleCotizacion;
import com.lacasadelchef.erp.entity.ServicioCotizacion;
import com.lacasadelchef.erp.repository.CotizacionVersionRepository;
import com.lacasadelchef.erp.repository.DetalleCotizacionRepository;
import com.lacasadelchef.erp.repository.ServicioCotizacionRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.List;

/** Arma el PDF de una version de cotizacion para entregarle al cliente. */
@Service
@RequiredArgsConstructor
public class CotizacionPdfService {

    private static final DateTimeFormatter FORMATO_FECHA_CORTA = DateTimeFormatter.ofPattern("d/M/yyyy");
    private static final Locale LOCALE_ES = Locale.of("es", "GT");
    /** Contacto que se imprime en el encabezado de cada pagina. */
    private static final String TELEFONO_EMPRESA = "5173-0435";

    private static final Color NAVY = new Color(0x2c, 0x3e, 0x50);
    private static final Color VERDE = new Color(0x27, 0xae, 0x60);
    private static final Color GRIS_ENCABEZADO = new Color(0xe9, 0xed, 0xf1);
    private static final Color GRIS_ZEBRA = new Color(0xf7, 0xf9, 0xfa);

    private final CotizacionVersionRepository cotizacionVersionRepository;
    private final DetalleCotizacionRepository detalleCotizacionRepository;
    private final ServicioCotizacionRepository servicioCotizacionRepository;
    private final CondicionesComerciales condiciones;

    @Transactional(readOnly = true)
    public byte[] generar(Integer idCotizacionVersion) {
        CotizacionVersion version = cotizacionVersionRepository.findById(idCotizacionVersion)
                .orElseThrow(() -> new ResourceNotFoundException("CotizacionVersion", idCotizacionVersion));
        List<DetalleCotizacion> detalles =
                detalleCotizacionRepository.findByCotizacionVersionIdCotizacionVersion(idCotizacionVersion);
        List<ServicioCotizacion> servicios =
                servicioCotizacionRepository.findByCotizacionVersionIdCotizacionVersion(idCotizacionVersion);
        Cotizacion cotizacion = version.getCotizacion();
        Cliente cliente = cotizacion.getCliente();

        Font fuenteTituloGrande = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
        Font fuenteSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font fuenteTexto = FontFactory.getFont(FontFactory.HELVETICA, 11);
        Font fuenteEncabezadoTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font fuenteCelda = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font fuenteResumenTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.WHITE);
        Font fuenteNota = FontFactory.getFont(FontFactory.HELVETICA, 9);

        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            Document documento = new Document(PageSize.LETTER, 40, 40, 115, 65);
            PdfWriter writer = PdfWriter.getInstance(documento, salida);
            writer.setPageEvent(new EncabezadoPiePagina());
            documento.open();

            Paragraph tituloCotizacion = new Paragraph("COTIZACIÓN", fuenteTituloGrande);
            tituloCotizacion.setSpacingAfter(10);
            documento.add(tituloCotizacion);

            String numeroCotizacion = "COT-%d-%03d".formatted(
                    cotizacion.getFechaCotizacion().getYear(), cotizacion.getIdCotizacion());
            documento.add(new Paragraph("No. Cotización: " + numeroCotizacion, fuenteTexto));
            documento.add(new Paragraph(
                    "Fecha: " + cotizacion.getFechaCotizacion().format(FORMATO_FECHA_CORTA), fuenteTexto));
            Paragraph vigencia = new Paragraph("Vigencia: %d días".formatted(condiciones.vigenciaDias()), fuenteTexto);
            vigencia.setSpacingAfter(16);
            documento.add(vigencia);

            documento.add(tituloSeccion("DATOS DEL CLIENTE", fuenteSeccion));
            documento.add(new Paragraph("Nombre: " + cliente.getNombre(), fuenteTexto));
            documento.add(new Paragraph("Teléfono: " + valorOTexto(cliente.getTelefono()), fuenteTexto));
            Paragraph correo = new Paragraph("Correo: " + valorOTexto(cliente.getCorreo()), fuenteTexto);
            correo.setSpacingAfter(16);
            documento.add(correo);

            documento.add(tituloSeccion("DATOS DEL EVENTO", fuenteSeccion));
            documento.add(new Paragraph("Tipo de evento: " + cotizacion.getTipoEvento().getNombreTipo(), fuenteTexto));
            documento.add(new Paragraph(
                    "Fecha del evento: " + formatearFechaEvento(cotizacion.getFechaEvento()), fuenteTexto));
            documento.add(new Paragraph("Horario del servicio: " + formatearHorario(cotizacion.getHoraInicio()), fuenteTexto));
            documento.add(new Paragraph("Ubicación: " + cotizacion.getUbicacion().getDireccion(), fuenteTexto));
            Paragraph personas = new Paragraph(
                    "Cantidad de personas: " + cotizacion.getCantidadPersonas(), fuenteTexto);
            personas.setSpacingAfter(16);
            documento.add(personas);

            documento.add(tituloSeccion("DETALLE DE SERVICIOS", fuenteSeccion));

            PdfPTable tabla = new PdfPTable(new float[] {4f, 1.1f, 1.4f, 1.4f});
            tabla.setWidthPercentage(100);
            tabla.setSpacingAfter(4);

            agregarCeldaEncabezado(tabla, "Descripción", fuenteEncabezadoTabla);
            agregarCeldaEncabezado(tabla, "Cant.", fuenteEncabezadoTabla);
            agregarCeldaEncabezado(tabla, "Precio Unit.", fuenteEncabezadoTabla);
            agregarCeldaEncabezado(tabla, "Total", fuenteEncabezadoTabla);

            int fila = 0;
            for (DetalleCotizacion detalle : detalles) {
                Color fondo = fondoZebra(fila++);
                String descripcion = detalle.getMenu().getNombreMenu() + " - " + detalle.getPlato().getNombrePlato()
                        + (detalle.getBebida() != null ? " (bebida: " + detalle.getBebida() + ")" : "");
                tabla.addCell(celda(descripcion, fuenteCelda, Element.ALIGN_LEFT, fondo));
                tabla.addCell(celda(String.valueOf(detalle.getCantidadPlatos()), fuenteCelda, Element.ALIGN_RIGHT, fondo));
                tabla.addCell(celda(formatearMoneda(detalle.getPrecioUnitario()), fuenteCelda, Element.ALIGN_RIGHT, fondo));
                tabla.addCell(celda(formatearMoneda(detalle.getSubtotal()), fuenteCelda, Element.ALIGN_RIGHT, fondo));
            }
            for (ServicioCotizacion servicio : servicios) {
                Color fondo = fondoZebra(fila++);
                String descripcion = servicio.getDescripcion() != null && !servicio.getDescripcion().isBlank()
                        ? servicio.getDescripcion()
                        : servicio.getTipoServicio().getNombreTipo();
                BigDecimal precioUnidad = servicio.getMonto()
                        .divide(BigDecimal.valueOf(servicio.getCantidad()), 2, RoundingMode.HALF_UP);
                tabla.addCell(celda(descripcion, fuenteCelda, Element.ALIGN_LEFT, fondo));
                tabla.addCell(celda(String.valueOf(servicio.getCantidad()), fuenteCelda, Element.ALIGN_RIGHT, fondo));
                tabla.addCell(celda(formatearMoneda(precioUnidad), fuenteCelda, Element.ALIGN_RIGHT, fondo));
                tabla.addCell(celda(formatearMoneda(servicio.getMonto()), fuenteCelda, Element.ALIGN_RIGHT, fondo));
            }

            documento.add(tabla);

            // Los precios del menu ya incluyen IVA: no se desglosa, se aclara debajo del total.
            BigDecimal total = version.getMontoTotal() != null ? version.getMontoTotal() : BigDecimal.ZERO;

            PdfPTable resumen = new PdfPTable(new float[] {3f, 2f});
            resumen.setWidthPercentage(45);
            resumen.setHorizontalAlignment(Element.ALIGN_RIGHT);
            resumen.setSpacingBefore(6);
            resumen.setSpacingAfter(4);

            agregarFilaResumen(resumen, "TOTAL:", formatearMoneda(total), fuenteResumenTotal, VERDE, 8);

            documento.add(resumen);
            Paragraph incluyeIva = new Paragraph("Precios incluyen IVA", fuenteNota);
            incluyeIva.setAlignment(Element.ALIGN_RIGHT);
            incluyeIva.setSpacingAfter(20);
            documento.add(incluyeIva);

            documento.add(tituloSeccion("CONDICIONES", fuenteSeccion));
            Font fuenteSubtituloNota = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            agregarCondicion(documento, "Vigencia: ", condiciones.textoVigencia(), fuenteSubtituloNota, fuenteNota);
            agregarCondicion(documento, "Forma de pago: ", CondicionesComerciales.FORMA_DE_PAGO,
                    fuenteSubtituloNota, fuenteNota);
            agregarCondicion(documento, "Horario: ", CondicionesComerciales.HORARIO, fuenteSubtituloNota, fuenteNota);
            agregarLista(documento, "El servicio incluye:", CondicionesComerciales.EL_SERVICIO_INCLUYE,
                    fuenteSubtituloNota, fuenteNota);
            agregarLista(documento, "Otras condiciones:", CondicionesComerciales.OTRAS_CONDICIONES,
                    fuenteSubtituloNota, fuenteNota);

            documento.close();
            return salida.toByteArray();
        } catch (DocumentException ex) {
            throw new IllegalStateException("No se pudo generar el PDF de la cotizacion", ex);
        }
    }

    /** "Titulo: texto" en un parrafo, con el titulo en negrita. */
    private void agregarCondicion(Document documento, String titulo, String texto, Font fuenteTitulo, Font fuente)
            throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Phrase(titulo, fuenteTitulo));
        p.add(new Phrase(texto, fuente));
        p.setSpacingAfter(4);
        // Si no cabe al final de la pagina pasa entera a la siguiente, en vez de partirse.
        p.setKeepTogether(true);
        documento.add(p);
    }

    private void agregarLista(Document documento, String titulo, List<String> puntos, Font fuenteTitulo, Font fuente)
            throws DocumentException {
        documento.add(new Paragraph(titulo, fuenteTitulo));
        for (String punto : puntos) {
            Paragraph p = new Paragraph("- " + punto, fuente);
            p.setIndentationLeft(8);
            documento.add(p);
        }
        documento.add(new Paragraph(" ", fuente));
    }

    private Paragraph tituloSeccion(String texto, Font fuente) {
        Paragraph p = new Paragraph(texto, fuente);
        p.setSpacingBefore(2);
        p.setSpacingAfter(8);
        return p;
    }

    private String valorOTexto(String valor) {
        return valor == null || valor.isBlank() ? "No registrado" : valor;
    }

    private String formatearFechaEvento(LocalDate fecha) {
        if (fecha == null) {
            return "Por definir";
        }
        String mes = fecha.getMonth().getDisplayName(TextStyle.FULL, LOCALE_ES);
        mes = mes.substring(0, 1).toUpperCase(LOCALE_ES) + mes.substring(1);
        return "%d de %s, %d".formatted(fecha.getDayOfMonth(), mes, fecha.getYear());
    }

    /** "6:00 p.m. a 10:00 p.m.", o "Por definir" si aun no se eligio la hora. */
    private String formatearHorario(LocalTime inicio) {
        if (inicio == null) {
            return "Por definir";
        }
        return formatearHora(inicio) + " a " + formatearHora(CondicionesComerciales.horaFinServicio(inicio));
    }

    private static String formatearHora(LocalTime hora) {
        int h12 = hora.getHour() % 12 == 0 ? 12 : hora.getHour() % 12;
        return "%d:%02d %s".formatted(h12, hora.getMinute(), hora.getHour() < 12 ? "a.m." : "p.m.");
    }

    private Color fondoZebra(int indiceFila) {
        return indiceFila % 2 == 0 ? Color.WHITE : GRIS_ZEBRA;
    }

    private void agregarCeldaEncabezado(PdfPTable tabla, String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBackgroundColor(GRIS_ENCABEZADO);
        celda.setPadding(7);
        tabla.addCell(celda);
    }

    private PdfPCell celda(String texto, Font fuente, int alineacion, Color fondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(6);
        if (fondo != null) {
            celda.setBackgroundColor(fondo);
        }
        return celda;
    }

    private void agregarFilaResumen(PdfPTable tabla, String etiqueta, String valor, Font fuente, Color fondo, float padding) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, fuente));
        celdaEtiqueta.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaEtiqueta.setBorder(0);
        celdaEtiqueta.setPadding(padding);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fuente));
        celdaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaValor.setBorder(0);
        celdaValor.setPadding(padding);

        if (fondo != null) {
            celdaEtiqueta.setBackgroundColor(fondo);
            celdaValor.setBackgroundColor(fondo);
        }
        tabla.addCell(celdaEtiqueta);
        tabla.addCell(celdaValor);
    }

    private String formatearMoneda(BigDecimal monto) {
        BigDecimal valor = (monto == null ? BigDecimal.ZERO : monto).setScale(2, RoundingMode.HALF_UP);
        return String.format(Locale.US, "Q%,.2f", valor);
    }

    /** Dibuja la franja navy con el letterhead arriba y la franja de agradecimiento abajo, en cada pagina. */
    private static final class EncabezadoPiePagina extends PdfPageEventHelper {
        private static final float ALTURA_HEADER = 95f;
        private static final float ALTURA_FOOTER = 45f;

        private final Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.WHITE);
        private final Font fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.WHITE);
        private final Font fuenteContacto = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.WHITE);
        private final Font fuentePie = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.WHITE);

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            float anchoPagina = document.getPageSize().getWidth();
            float altoPagina = document.getPageSize().getHeight();

            cb.saveState();
            cb.setColorFill(NAVY);
            cb.rectangle(0, altoPagina - ALTURA_HEADER, anchoPagina, ALTURA_HEADER);
            cb.fill();
            cb.rectangle(0, 0, anchoPagina, ALTURA_FOOTER);
            cb.fill();
            cb.restoreState();

            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase("LA CASA DEL CHEF", fuenteTitulo), anchoPagina / 2, altoPagina - 35, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase("Servicios de Banquetes y Eventos", fuenteSubtitulo), anchoPagina / 2, altoPagina - 55, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase("Tel: " + TELEFONO_EMPRESA, fuenteContacto), anchoPagina / 2, altoPagina - 72, 0);

            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase("Gracias por su preferencia | La Casa del Chef", fuentePie),
                    anchoPagina / 2, ALTURA_FOOTER / 2f - 3, 0);
        }
    }
}
