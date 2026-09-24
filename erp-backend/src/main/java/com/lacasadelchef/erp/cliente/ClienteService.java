package com.lacasadelchef.erp.cliente;

import com.lacasadelchef.erp.cliente.dto.ClienteRequest;
import com.lacasadelchef.erp.cliente.dto.ClienteResponse;
import com.lacasadelchef.erp.cliente.dto.PosibleDuplicadoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClienteService {

    /** estado: "ACTIVO", "INACTIVO" o null/vacio para todos. */
    Page<ClienteResponse> listar(String busqueda, String estado, Pageable pageable);

    ClienteResponse obtenerPorId(Integer id);

    ClienteResponse crear(ClienteRequest request);

    ClienteResponse actualizar(Integer id, ClienteRequest request);

    /** Inactivar o reactivar. Un cliente no se borra (ver V20). */
    ClienteResponse cambiarEstado(Integer id, boolean activo);

    List<PosibleDuplicadoResponse> posiblesDuplicados(String nombre, String nit, String telefono, String correo,
                                                      Integer idExcluir);
}
