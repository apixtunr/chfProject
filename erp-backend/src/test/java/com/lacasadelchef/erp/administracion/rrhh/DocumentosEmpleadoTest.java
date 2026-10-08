package com.lacasadelchef.erp.administracion.rrhh;

import com.lacasadelchef.erp.administracion.rrhh.dto.DocumentoEmpleadoRequest;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.entity.DocumentoEmpleado;
import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.TipoDocumento;
import com.lacasadelchef.erp.entity.id.DocumentoEmpleadoId;
import com.lacasadelchef.erp.repository.DocumentoEmpleadoRepository;
import com.lacasadelchef.erp.repository.EmpleadoRepository;
import com.lacasadelchef.erp.repository.TipoDocumentoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Reglas de los documentos del empleado: el formato del DPI, que un numero no quede en
 * dos personas, y que la API no reemplace en silencio ni deje a alguien sin DPI.
 */
class DocumentosEmpleadoTest {

    static TipoDocumento tipo(int id, String nombre) {
        TipoDocumento tipo = new TipoDocumento();
        tipo.setIdTipoDocumento(id);
        tipo.setNombreTipo(nombre);
        return tipo;
    }

    static Empleado empleado(int id, String nombre, String apellido) {
        Empleado empleado = new Empleado();
        empleado.setIdEmpleado(id);
        empleado.setNombre(nombre);
        empleado.setApellido(apellido);
        return empleado;
    }

    static DocumentoEmpleado documento(Empleado empleado, TipoDocumento tipo, String numero) {
        DocumentoEmpleado documento = new DocumentoEmpleado();
        documento.setId(new DocumentoEmpleadoId(empleado.getIdEmpleado(), tipo.getIdTipoDocumento()));
        documento.setEmpleado(empleado);
        documento.setTipoDocumento(tipo);
        documento.setNumeroDocumento(numero);
        return documento;
    }

    private static final TipoDocumento DPI = tipo(1, "DPI");
    private static final TipoDocumento LICENCIA = tipo(2, "Licencia de conducir");

    @Test
    @DisplayName("El DPI se guarda sin espacios ni guiones, como se escribe en el documento")
    void dpiNormalizado() {
        assertThat(DocumentosEmpleado.normalizar(DPI, " 2547 12345-0101 ")).isEqualTo("2547123450101");
    }

    @Test
    @DisplayName("Un DPI que no tiene 13 digitos se rechaza")
    void dpiInvalido() {
        assertThatThrownBy(() -> DocumentosEmpleado.normalizar(DPI, "2547 1234"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("El DPI debe tener 13 dígitos");
        assertThatThrownBy(() -> DocumentosEmpleado.normalizar(DPI, "2547A23450101"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Los demas documentos solo se recortan; no pueden venir vacios")
    void otrosDocumentos() {
        assertThat(DocumentosEmpleado.normalizar(LICENCIA, " A-12345 ")).isEqualTo("A-12345");
        assertThatThrownBy(() -> DocumentosEmpleado.normalizar(LICENCIA, "   "))
                .isInstanceOf(BusinessException.class)
                .hasMessage("El número de Licencia de conducir es obligatorio");
    }

    @Test
    @DisplayName("DPI y licencia son tipos del sistema; los demas no")
    void tiposDelSistema() {
        assertThat(DocumentosEmpleado.esDelSistema("dpi")).isTrue();
        assertThat(DocumentosEmpleado.esDelSistema("Licencia de conducir")).isTrue();
        assertThat(DocumentosEmpleado.esDelSistema("Afiliación IGSS")).isFalse();
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class NoRepetido {

        @Mock private DocumentoEmpleadoRepository documentoEmpleadoRepository;
        @Mock private TipoDocumentoRepository tipoDocumentoRepository;
        @InjectMocks private DocumentosEmpleado documentos;

        @Test
        @DisplayName("Un DPI que ya tiene otro empleado se rechaza, diciendo de quien es")
        void dpiDeOtro() {
            Empleado otro = empleado(8, "Lucía", "Ramírez");
            when(documentoEmpleadoRepository.findByTipoDocumentoIdTipoDocumentoAndNumeroDocumento(1, "2547123450101"))
                    .thenReturn(Optional.of(documento(otro, DPI, "2547123450101")));

            assertThatThrownBy(() -> documentos.guardar(empleado(5, "Amado", "Soto"), DPI, "2547 12345 0101"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("El DPI 2547123450101 ya está registrado para Lucía Ramírez");
            verify(documentoEmpleadoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Volver a guardar el mismo DPI del mismo empleado no es repetido")
        void mismoEmpleado() {
            Empleado amado = empleado(5, "Amado", "Soto");
            DocumentoEmpleado suyo = documento(amado, DPI, "2547123450101");
            when(documentoEmpleadoRepository.findByTipoDocumentoIdTipoDocumentoAndNumeroDocumento(1, "2547123450101"))
                    .thenReturn(Optional.of(suyo));
            when(documentoEmpleadoRepository.findById(suyo.getId())).thenReturn(Optional.of(suyo));
            when(documentoEmpleadoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertThat(documentos.guardar(amado, DPI, "2547123450101").getNumeroDocumento()).isEqualTo("2547123450101");
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class Api {

        @Mock private DocumentoEmpleadoRepository documentoEmpleadoRepository;
        @Mock private EmpleadoRepository empleadoRepository;
        @Mock private TipoDocumentoRepository tipoDocumentoRepository;
        @Mock private DocumentosEmpleado documentosEmpleado;
        @InjectMocks private DocumentoEmpleadoServiceImpl service;

        @Test
        @DisplayName("Agregar un tipo que el empleado ya tiene se rechaza en vez de reemplazarlo en silencio")
        void agregarRepetido() {
            when(empleadoRepository.findById(5)).thenReturn(Optional.of(empleado(5, "Amado", "Soto")));
            when(tipoDocumentoRepository.findById(2)).thenReturn(Optional.of(LICENCIA));
            when(documentoEmpleadoRepository.existsById(new DocumentoEmpleadoId(5, 2))).thenReturn(true);

            assertThatThrownBy(() -> service.agregar(5, 2, new DocumentoEmpleadoRequest("A-999")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ya tiene registrado su Licencia de conducir");
            verify(documentosEmpleado, never()).guardar(any(), any(), any());
        }

        @Test
        @DisplayName("El DPI no se puede quitar: es obligatorio")
        void noQuitaDpi() {
            Empleado amado = empleado(5, "Amado", "Soto");
            when(documentoEmpleadoRepository.findById(new DocumentoEmpleadoId(5, 1)))
                    .thenReturn(Optional.of(documento(amado, DPI, "2547123450101")));

            assertThatThrownBy(() -> service.eliminar(5, 1))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("obligatorio");
            verify(documentoEmpleadoRepository, never()).delete(any());
        }

        @Test
        @DisplayName("La licencia si se puede quitar")
        void quitaLicencia() {
            Empleado amado = empleado(5, "Amado", "Soto");
            DocumentoEmpleado licencia = documento(amado, LICENCIA, "A-123");
            when(documentoEmpleadoRepository.findById(new DocumentoEmpleadoId(5, 2))).thenReturn(Optional.of(licencia));

            service.eliminar(5, 2);

            verify(documentoEmpleadoRepository).delete(licencia);
        }
    }
}
