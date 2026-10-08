package com.lacasadelchef.erp.administracion.rrhh;

import com.lacasadelchef.erp.administracion.rrhh.dto.DocumentoEmpleadoRequest;
import com.lacasadelchef.erp.administracion.rrhh.dto.DocumentoEmpleadoResponse;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.DocumentoEmpleado;
import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.TipoDocumento;
import com.lacasadelchef.erp.entity.id.DocumentoEmpleadoId;
import com.lacasadelchef.erp.repository.DocumentoEmpleadoRepository;
import com.lacasadelchef.erp.repository.EmpleadoRepository;
import com.lacasadelchef.erp.repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentoEmpleadoServiceImpl implements DocumentoEmpleadoService {

    private final DocumentoEmpleadoRepository documentoEmpleadoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final DocumentosEmpleado documentosEmpleado;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoEmpleadoResponse> listar(Integer idEmpleado) {
        return documentoEmpleadoRepository.findByEmpleadoIdEmpleado(idEmpleado).stream()
                .map(DocumentoEmpleadoResponse::desde)
                .toList();
    }

    @Override
    @Transactional
    public DocumentoEmpleadoResponse agregar(Integer idEmpleado, Integer idTipoDocumento, DocumentoEmpleadoRequest request) {
        Empleado empleado = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado", idEmpleado));
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(idTipoDocumento)
                .orElseThrow(() -> new ResourceNotFoundException("TipoDocumento", idTipoDocumento));
        // Sin esto, guardar con la misma llave reemplazaba en silencio el documento anterior.
        if (documentoEmpleadoRepository.existsById(new DocumentoEmpleadoId(idEmpleado, idTipoDocumento))) {
            throw new BusinessException("%s ya tiene registrado su %s; modifíquelo en lugar de agregarlo otra vez"
                    .formatted(empleado.getNombreCompleto(), tipoDocumento.getNombreTipo()));
        }
        return DocumentoEmpleadoResponse.desde(documentosEmpleado.guardar(empleado, tipoDocumento, request.numeroDocumento()));
    }

    @Override
    @Transactional
    public DocumentoEmpleadoResponse actualizar(Integer idEmpleado, Integer idTipoDocumento, DocumentoEmpleadoRequest request) {
        DocumentoEmpleado documento = buscar(idEmpleado, idTipoDocumento);
        return DocumentoEmpleadoResponse.desde(documentosEmpleado.guardar(
                documento.getEmpleado(), documento.getTipoDocumento(), request.numeroDocumento()));
    }

    @Override
    @Transactional
    public void eliminar(Integer idEmpleado, Integer idTipoDocumento) {
        DocumentoEmpleado documento = buscar(idEmpleado, idTipoDocumento);
        if (DocumentosEmpleado.esDpi(documento.getTipoDocumento())) {
            throw new BusinessException("El DPI es obligatorio: se puede corregir, pero no quitar");
        }
        documentoEmpleadoRepository.delete(documento);
    }

    private DocumentoEmpleado buscar(Integer idEmpleado, Integer idTipoDocumento) {
        return documentoEmpleadoRepository.findById(new DocumentoEmpleadoId(idEmpleado, idTipoDocumento))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El empleado %d no tiene registrado el tipo de documento %d".formatted(idEmpleado, idTipoDocumento)));
    }
}
