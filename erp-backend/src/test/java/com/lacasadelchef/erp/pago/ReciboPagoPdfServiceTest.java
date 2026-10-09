package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.entity.CostoEvento;
import com.lacasadelchef.erp.entity.Cotizacion;
import com.lacasadelchef.erp.entity.CotizacionVersion;
import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.MetodoPago;
import com.lacasadelchef.erp.entity.Pago;
import com.lacasadelchef.erp.entity.TipoCosto;
import com.lacasadelchef.erp.entity.TipoEvento;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.repository.PagoRepository;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** El recibo de pago: lo que el cliente se lleva cuando entrega el dinero. */
@ExtendWith(MockitoExtension.class)
class ReciboPagoPdfServiceTest {

    @Mock private PagoRepository pagoRepository;
    @InjectMocks private ReciboPagoPdfService service;

    private static Estado estado(String nombre) {
        Estado estado = new Estado();
        estado.setNombre(nombre);
        return estado;
    }

    /** Abono en efectivo de Q1,500 a un evento de Q4,000 que ya tenia Q1,000 abonados. */
    private static Pago abonoEnEfectivo(String estadoPago) {
        Cliente cliente = new Cliente();
        cliente.setNombre("María Fernanda López");
        cliente.setNit("4589632-1");
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setCliente(cliente);
        CotizacionVersion version = new CotizacionVersion();
        version.setCotizacion(cotizacion);
        version.setMontoTotal(new BigDecimal("4000.00"));
        TipoEvento tipo = new TipoEvento();
        tipo.setNombreTipo("Boda");
        Evento evento = new Evento();
        evento.setIdEvento(45);
        evento.setTipoEvento(tipo);
        evento.setFechaEvento(LocalDate.of(2026, 10, 15));
        evento.setCotizacionVersion(version);
        MetodoPago efectivo = new MetodoPago();
        efectivo.setNombreMetodo("Efectivo");
        Empleado empleado = new Empleado();
        empleado.setNombre("Yesenia Marleny");
        empleado.setApellido("Ajtún");
        Usuario usuario = new Usuario();
        usuario.setUsername("yesenia.ajtun");
        usuario.setEmpleado(empleado);

        Pago pago = new Pago();
        pago.setIdPago(7);
        pago.setEvento(evento);
        pago.setMetodoPago(efectivo);
        pago.setUsuario(usuario);
        pago.setEstado(estado(estadoPago));
        pago.setMonto(new BigDecimal("1500.00"));
        pago.setFechaPago(LocalDateTime.of(2026, 10, 8, 10, 30));
        return pago;
    }

    private static String texto(byte[] pdf) throws IOException {
        PdfReader lector = new PdfReader(pdf);
        StringBuilder texto = new StringBuilder();
        PdfTextExtractor extractor = new PdfTextExtractor(lector);
        for (int pagina = 1; pagina <= lector.getNumberOfPages(); pagina++) {
            texto.append(extractor.getTextFromPage(pagina)).append('\n');
        }
        return texto.toString();
    }

    @Test
    @DisplayName("El recibo de un abono trae monto en letras, cliente, saldo a ese momento y la nota de IVA")
    void abono() throws IOException {
        when(pagoRepository.sumarAbonadoHasta(45, 7)).thenReturn(new BigDecimal("2500.00"));

        Pago pago = abonoEnEfectivo("CONFIRMADO");
        // Con todo lo opcional lleno: es el caso que antes se pasaba a una segunda hoja.
        pago.setReferenciaTransaccion("TRX-889911");
        pago.setObservaciones("Entregado en oficina por el padre de la novia, segundo abono");
        when(pagoRepository.findById(7)).thenReturn(Optional.of(pago));

        byte[] pdf = service.generar(7);
        // Copia para revisarlo a ojo (target/ no se versiona).
        Files.write(Path.of("target", "recibo-ejemplo.pdf"), pdf);
        String texto = texto(pdf);

        PdfReader lector = new PdfReader(pdf);
        assertThat(lector.getNumberOfPages()).as("el recibo cabe en una hoja").isEqualTo(1);
        lector.close();

        assertThat(texto)
                .contains("RECIBO DE PAGO", "REC-000007", "08/10/2026 10:30")
                .contains("María Fernanda López", "4589632-1")
                .contains("Q1,500.00", "Un mil quinientos quetzales con 00/100")
                .contains("Abono al evento #45", "Boda")
                .contains("Efectivo")
                .contains("Q4,000.00", "Q2,500.00", "Q1,500.00")
                .contains("Precios incluyen IVA", "Yesenia Marleny Ajtún", "No es un documento fiscal");
    }

    @Test
    @DisplayName("Un pago registrado solo con el dia no imprime la hora 00:00")
    void fechaSinHora() {
        assertThat(ReciboPagoPdfService.formatearFechaPago(LocalDateTime.of(2026, 10, 8, 0, 0))).isEqualTo("08/10/2026");
        assertThat(ReciboPagoPdfService.formatearFechaPago(LocalDateTime.of(2026, 10, 8, 10, 30))).isEqualTo("08/10/2026 10:30");
    }

    @Test
    @DisplayName("Un pago anulado sale con la marca ANULADO y sin estado de cuenta")
    void anulado() throws IOException {
        when(pagoRepository.findById(7)).thenReturn(Optional.of(abonoEnEfectivo("ANULADO")));

        String texto = texto(service.generar(7));

        assertThat(texto).contains("ANULADO").doesNotContain("ESTADO DE CUENTA");
        verify(pagoRepository, never()).sumarAbonadoHasta(45, 7);
    }

    @Test
    @DisplayName("El reembolso de un costo extra dice que costo es y no toca el saldo del evento")
    void reembolsoDeCosto() throws IOException {
        Pago pago = abonoEnEfectivo("CONFIRMADO");
        TipoCosto transporte = new TipoCosto();
        transporte.setNombreTipo("Transporte");
        CostoEvento costo = new CostoEvento();
        costo.setTipoCosto(transporte);
        pago.setCostoEvento(costo);
        when(pagoRepository.findById(7)).thenReturn(Optional.of(pago));

        String texto = texto(service.generar(7));

        assertThat(texto).contains("Reembolso de costo extra (Transporte)").doesNotContain("ESTADO DE CUENTA");
    }
}
