package com.lacasadelchef.erp.administracion.usuario;

import com.lacasadelchef.erp.administracion.usuario.dto.CambiarPasswordRequest;
import com.lacasadelchef.erp.administracion.usuario.dto.UsuarioActualizarRequest;
import com.lacasadelchef.erp.administracion.usuario.dto.UsuarioRequest;
import com.lacasadelchef.erp.administracion.usuario.dto.UsuarioResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioService {

    Page<UsuarioResponse> listar(String username, Pageable pageable);

    UsuarioResponse obtenerPorId(Integer id);

    UsuarioResponse crear(UsuarioRequest request);

    UsuarioResponse actualizar(Integer id, UsuarioActualizarRequest request);

    void eliminar(Integer id);

    void cambiarPassword(Integer id, CambiarPasswordRequest request);

    UsuarioResponse desbloquear(Integer id);
}
