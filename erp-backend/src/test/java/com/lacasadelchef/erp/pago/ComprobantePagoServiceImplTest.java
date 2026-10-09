package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.entity.ComprobanteArchivo;
import com.lacasadelchef.erp.entity.ComprobantePago;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Pago;
import com.lacasadelchef.erp.pago.dto.ComprobantePagoRequest;
import com.lacasadelchef.erp.repository.ComprobanteArchivoRepository;
import com.lacasadelchef.erp.repository.ComprobantePagoRepository;
import com.lacasadelchef.erp.repository.PagoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Comprobantes de pago: que el archivo sea de verdad un PDF o una imagen, que no se
 * respalde un pago anulado y que una misma boleta no respalde dos pagos.
 */
@ExtendWith(MockitoExtension.class)
class ComprobantePagoServiceImplTest {

    private static final byte[] PDF = "%PDF-1.7 contenido".getBytes();
    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 1, 2, 3};
    private static final byte[] EXE = {'M', 'Z', (byte) 0x90, 0, 3, 0};

    @Mock private ComprobantePagoRepository comprobantePagoRepository;
    @Mock private ComprobanteArchivoRepository comprobanteArchivoRepository;
    @Mock private PagoRepository pagoRepository;
    @InjectMocks private ComprobantePagoServiceImpl service;

    private static Pago pago(int id, String estado) {
        Estado e = new Estado();
        e.setNombre(estado);
        Pago pago = new Pago();
        pago.setIdPago(id);
        pago.setEstado(e);
        return pago;
    }

    private static ComprobantePagoRequest datos(String tipo, String numero) {
        return new ComprobantePagoRequest(numero, null, tipo, LocalDate.of(2026, 10, 8), null);
    }

    private void comprobanteSeGraba() {
        when(comprobantePagoRepository.save(any(ComprobantePago.class))).thenAnswer(inv -> {
            ComprobantePago c = inv.getArgument(0);
            if (c.getIdComprobante() == null) {
                c.setIdComprobante(30);
            }
            return c;
        });
    }

    @Test
    @DisplayName("Con la foto de la boleta se guardan el comprobante y su archivo, y archivo_url apunta a la descarga")
    void agregaConArchivo() {
        when(pagoRepository.findById(1)).thenReturn(Optional.of(pago(1, "CONFIRMADO")));
        comprobanteSeGraba();
        when(comprobanteArchivoRepository.save(any(ComprobanteArchivo.class))).thenAnswer(inv -> inv.getArgument(0));

        var respuesta = service.agregar(1, datos("Boleta de depósito", " 778899 "),
                new MockMultipartFile("archivo", "C:\\fotos\\boleta.jpg", "image/jpeg", JPG));

        ArgumentCaptor<ComprobanteArchivo> guardado = ArgumentCaptor.forClass(ComprobanteArchivo.class);
        verify(comprobanteArchivoRepository).save(guardado.capture());
        assertThat(guardado.getValue().getTipoContenido()).isEqualTo("image/jpeg");
        assertThat(guardado.getValue().getNombreArchivo()).isEqualTo("boleta.jpg");
        assertThat(guardado.getValue().getTamanoBytes()).isEqualTo(JPG.length);
        assertThat(respuesta.numeroComprobante()).isEqualTo("778899");
        assertThat(respuesta.nombreArchivo()).isEqualTo("boleta.jpg");
        assertThat(respuesta.archivoUrl()).isEqualTo("/api/pagos/1/comprobantes/30/archivo");
    }

    @Test
    @DisplayName("Un archivo que no es PDF ni imagen se rechaza aunque diga .pdf, y no se graba nada")
    void archivoFalso() {
        when(pagoRepository.findById(1)).thenReturn(Optional.of(pago(1, "CONFIRMADO")));

        assertThatThrownBy(() -> service.agregar(1, datos("Recibo", "R-1"),
                new MockMultipartFile("archivo", "recibo.pdf", "application/pdf", EXE)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("El comprobante tiene que ser un PDF o una imagen JPG o PNG");
        verify(comprobantePagoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Un archivo de mas de 5 MB se rechaza")
    void archivoGrande() {
        when(pagoRepository.findById(1)).thenReturn(Optional.of(pago(1, "CONFIRMADO")));
        byte[] grande = new byte[ArchivoComprobante.TAMANO_MAXIMO + 1];
        System.arraycopy(PDF, 0, grande, 0, PDF.length);

        assertThatThrownBy(() -> service.agregar(1, datos("Factura", "F-1"),
                new MockMultipartFile("archivo", "factura.pdf", "application/pdf", grande)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("El archivo pesa más de 5 MB");
    }

    @Test
    @DisplayName("A un pago anulado no se le agregan comprobantes")
    void pagoAnulado() {
        when(pagoRepository.findById(1)).thenReturn(Optional.of(pago(1, "ANULADO")));

        assertThatThrownBy(() -> service.agregar(1, datos("Recibo", "R-1"), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("anulado");
        verify(comprobantePagoRepository, never()).save(any());
    }

    @Test
    @DisplayName("El tipo de comprobante tiene que ser uno de la lista")
    void tipoInvalido() {
        when(pagoRepository.findById(1)).thenReturn(Optional.of(pago(1, "CONFIRMADO")));

        assertThatThrownBy(() -> service.agregar(1, datos("Cheque", "C-1"), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageStartingWith("Tipo de comprobante no válido");
    }

    @Test
    @DisplayName("Una misma boleta no puede respaldar dos pagos")
    void boletaRepetida() {
        when(pagoRepository.findById(2)).thenReturn(Optional.of(pago(2, "CONFIRMADO")));
        ComprobantePago otro = new ComprobantePago();
        otro.setIdComprobante(10);
        otro.setPago(pago(1, "CONFIRMADO"));
        when(comprobantePagoRepository.findByTipoComprobanteAndNumeroComprobante("Boleta de depósito", "778899"))
                .thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> service.agregar(2, datos("Boleta de depósito", "778899"), null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("El comprobante Boleta de depósito 778899 ya respalda el pago #1");
    }

    @Test
    @DisplayName("Reemplazar el archivo lo actualiza sobre el mismo registro")
    void reemplazaArchivo() {
        ComprobantePago comprobante = new ComprobantePago();
        comprobante.setIdComprobante(30);
        comprobante.setPago(pago(1, "CONFIRMADO"));
        when(comprobantePagoRepository.findById(30)).thenReturn(Optional.of(comprobante));
        ComprobanteArchivo anterior = new ComprobanteArchivo();
        anterior.setIdComprobante(30);
        anterior.setNombreArchivo("borrosa.jpg");
        anterior.setTipoContenido("image/jpeg");
        when(comprobanteArchivoRepository.findById(30)).thenReturn(Optional.of(anterior));
        when(comprobanteArchivoRepository.save(any(ComprobanteArchivo.class))).thenAnswer(inv -> inv.getArgument(0));

        var respuesta = service.guardarArchivo(1, 30, new MockMultipartFile("archivo", "boleta.pdf", "application/pdf", PDF));

        assertThat(anterior.getNombreArchivo()).isEqualTo("boleta.pdf");
        assertThat(anterior.getTipoContenido()).isEqualTo("application/pdf");
        assertThat(respuesta.tipoContenido()).isEqualTo("application/pdf");
    }
}
