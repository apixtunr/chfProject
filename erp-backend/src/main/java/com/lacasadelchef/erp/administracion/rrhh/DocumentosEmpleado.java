package com.lacasadelchef.erp.administracion.rrhh;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.entity.DocumentoEmpleado;
import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.TipoDocumento;
import com.lacasadelchef.erp.entity.id.DocumentoEmpleadoId;
import com.lacasadelchef.erp.repository.DocumentoEmpleadoRepository;
import com.lacasadelchef.erp.repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Reglas de los documentos de un empleado, compartidas por el formulario del empleado
 * (que guarda DPI y documentos junto con sus datos) y por la API de documentos.
 *
 * Dos tipos tienen reglas propias y el sistema los reconoce por su nombre (V31):
 *   - DPI: obligatorio, 13 digitos. Se registra en los datos del empleado.
 *   - Licencia de conducir: la exige la asignacion de conductor de un vehiculo.
 * Por eso esos dos no se pueden renombrar ni borrar desde el catalogo.
 */
@Component
@RequiredArgsConstructor
public class DocumentosEmpleado {

    public static final String TIPO_DPI = "DPI";
    public static final String TIPO_LICENCIA = "Licencia de conducir";
    public static final Set<String> TIPOS_DEL_SISTEMA = Set.of(TIPO_DPI, TIPO_LICENCIA);

    private final DocumentoEmpleadoRepository documentoEmpleadoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;

    public static boolean esDelSistema(String nombreTipo) {
        return nombreTipo != null && TIPOS_DEL_SISTEMA.stream().anyMatch(t -> t.equalsIgnoreCase(nombreTipo.trim()));
    }

    public static boolean esDpi(TipoDocumento tipo) {
        return TIPO_DPI.equalsIgnoreCase(tipo.getNombreTipo());
    }

    public TipoDocumento tipoDpi() {
        return tipoDocumentoRepository.findByNombreTipo(TIPO_DPI)
                .orElseThrow(() -> new IllegalStateException("Falta el tipo de documento DPI (revisar V31)"));
    }

    /**
     * Deja el numero como se guarda: el DPI sin espacios ni guiones (asi se escribe en el
     * documento fisico, "2547 12345 0101"), los demas solo sin espacios a los lados.
     */
    public static String normalizar(TipoDocumento tipo, String numero) {
        String limpio = numero == null ? "" : numero.trim();
        if (esDpi(tipo)) {
            limpio = limpio.replaceAll("[\\s-]", "");
            if (!limpio.matches("\\d{13}")) {
                throw new BusinessException("El DPI debe tener 13 dígitos");
            }
        }
        if (limpio.isEmpty()) {
            throw new BusinessException("El número de %s es obligatorio".formatted(tipo.getNombreTipo()));
        }
        return limpio;
    }

    /** Un numero no puede pertenecer a dos empleados: serian la misma persona registrada dos veces. */
    public void validarNoRepetido(TipoDocumento tipo, String numero, Integer idEmpleado) {
        documentoEmpleadoRepository
                .findByTipoDocumentoIdTipoDocumentoAndNumeroDocumento(tipo.getIdTipoDocumento(), numero)
                .filter(otro -> !otro.getEmpleado().getIdEmpleado().equals(idEmpleado))
                .ifPresent(otro -> {
                    throw new BusinessException("El %s %s ya está registrado para %s"
                            .formatted(tipo.getNombreTipo(), numero, otro.getEmpleado().getNombreCompleto()));
                });
    }

    /** Crea o actualiza el documento de ese tipo del empleado, ya validado. */
    public DocumentoEmpleado guardar(Empleado empleado, TipoDocumento tipo, String numero) {
        String normalizado = normalizar(tipo, numero);
        validarNoRepetido(tipo, normalizado, empleado.getIdEmpleado());
        DocumentoEmpleadoId id = new DocumentoEmpleadoId(empleado.getIdEmpleado(), tipo.getIdTipoDocumento());
        DocumentoEmpleado documento = documentoEmpleadoRepository.findById(id).orElseGet(() -> {
            DocumentoEmpleado nuevo = new DocumentoEmpleado();
            nuevo.setId(id);
            nuevo.setEmpleado(empleado);
            nuevo.setTipoDocumento(tipo);
            return nuevo;
        });
        documento.setNumeroDocumento(normalizado);
        return documentoEmpleadoRepository.save(documento);
    }
}
