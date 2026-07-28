package com.lacasadelchef.erp.administracion.geografia;

import com.lacasadelchef.erp.administracion.geografia.dto.MunicipioRequest;
import com.lacasadelchef.erp.administracion.geografia.dto.MunicipioResponse;

import java.util.List;

public interface MunicipioService {

    List<MunicipioResponse> listar(Integer idDepartamento);

    MunicipioResponse obtenerPorId(Integer id);

    MunicipioResponse crear(MunicipioRequest request);

    MunicipioResponse actualizar(Integer id, MunicipioRequest request);

    void eliminar(Integer id);
}
