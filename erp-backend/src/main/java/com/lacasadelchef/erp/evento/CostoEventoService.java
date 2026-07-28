package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.evento.dto.CostoEventoRequest;
import com.lacasadelchef.erp.evento.dto.CostoEventoResponse;

import java.util.List;

public interface CostoEventoService {

    List<CostoEventoResponse> listar(Integer idEvento);

    CostoEventoResponse agregar(Integer idEvento, CostoEventoRequest request);

    CostoEventoResponse actualizar(Integer idEvento, Integer idCostoEvento, CostoEventoRequest request);

    void eliminar(Integer idEvento, Integer idCostoEvento);
}
