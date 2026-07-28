package com.lacasadelchef.erp.administracion.rrhh;

import com.lacasadelchef.erp.administracion.rrhh.dto.DocumentoEmpleadoRequest;
import com.lacasadelchef.erp.administracion.rrhh.dto.DocumentoEmpleadoResponse;

import java.util.List;

public interface DocumentoEmpleadoService {

    List<DocumentoEmpleadoResponse> listar(Integer idEmpleado);

    DocumentoEmpleadoResponse agregar(Integer idEmpleado, Integer idTipoDocumento, DocumentoEmpleadoRequest request);

    DocumentoEmpleadoResponse actualizar(Integer idEmpleado, Integer idTipoDocumento, DocumentoEmpleadoRequest request);

    void eliminar(Integer idEmpleado, Integer idTipoDocumento);
}
