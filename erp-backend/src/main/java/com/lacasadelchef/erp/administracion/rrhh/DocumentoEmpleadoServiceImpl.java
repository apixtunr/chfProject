package com.lacasadelchef.erp.administracion.rrhh;

import com.lacasadelchef.erp.administracion.rrhh.dto.DocumentoEmpleadoRequest;
import com.lacasadelchef.erp.administracion.rrhh.dto.DocumentoEmpleadoResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
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

    private static final String TABLA = "documento_empleado";

    private final DocumentoEmpleadoRepository documentoEmpleadoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

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

        DocumentoEmpleado documento = new DocumentoEmpleado();
        documento.setId(new DocumentoEmpleadoId(idEmpleado, idTipoDocumento));
        documento.setEmpleado(empleado);
        documento.setTipoDocumento(tipoDocumento);
        documento.setNumeroDocumento(request.numeroDocumento().trim());
        documento = documentoEmpleadoRepository.save(documento);
        bitacoraMovimientoService.registrar(TABLA, idEmpleado + "-" + idTipoDocumento, Operacion.INSERT);
        return DocumentoEmpleadoResponse.desde(documento);
    }

    @Override
    @Transactional
    public DocumentoEmpleadoResponse actualizar(Integer idEmpleado, Integer idTipoDocumento, DocumentoEmpleadoRequest request) {
        DocumentoEmpleado documento = buscar(idEmpleado, idTipoDocumento);
        documento.setNumeroDocumento(request.numeroDocumento().trim());
        documento = documentoEmpleadoRepository.save(documento);
        bitacoraMovimientoService.registrar(TABLA, idEmpleado + "-" + idTipoDocumento, Operacion.UPDATE);
        return DocumentoEmpleadoResponse.desde(documento);
    }

    @Override
    @Transactional
    public void eliminar(Integer idEmpleado, Integer idTipoDocumento) {
        DocumentoEmpleado documento = buscar(idEmpleado, idTipoDocumento);
        documentoEmpleadoRepository.delete(documento);
        bitacoraMovimientoService.registrar(TABLA, idEmpleado + "-" + idTipoDocumento, Operacion.DELETE);
    }

    private DocumentoEmpleado buscar(Integer idEmpleado, Integer idTipoDocumento) {
        return documentoEmpleadoRepository.findById(new DocumentoEmpleadoId(idEmpleado, idTipoDocumento))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El empleado %d no tiene registrado el tipo de documento %d".formatted(idEmpleado, idTipoDocumento)));
    }
}
