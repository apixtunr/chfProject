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

    public void validarNoRepetido(TipoDocumento tipo, String numero, Integer idEmpleado) {
        documentoEmpleadoRepository
                .findByTipoDocumentoIdTipoDocumentoAndNumeroDocumento(tipo.getIdTipoDocumento(), numero)
                .filter(otro -> !otro.getEmpleado().getIdEmpleado().equals(idEmpleado))
                .ifPresent(otro -> {
                    throw new BusinessException("El %s %s ya está registrado para %s"
                            .formatted(tipo.getNombreTipo(), numero, otro.getEmpleado().getNombreCompleto()));
                });
    }

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
