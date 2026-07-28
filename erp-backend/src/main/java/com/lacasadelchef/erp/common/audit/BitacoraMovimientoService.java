package com.lacasadelchef.erp.common.audit;

import com.lacasadelchef.erp.entity.BitacoraMovimiento;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.repository.BitacoraMovimientoRepository;
import com.lacasadelchef.erp.security.UsuarioPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra en bitacora_movimiento una fila por cada alta/baja/modificacion de negocio.
 * Se invoca explicitamente desde cada service, igual que AuthService hace con bitacora_acceso.
 */
@Service
@RequiredArgsConstructor
public class BitacoraMovimientoService {

    private final BitacoraMovimientoRepository bitacoraMovimientoRepository;

    @Transactional
    public void registrar(String tablaAfectada, Object registroId, Operacion operacion) {
        BitacoraMovimiento registro = new BitacoraMovimiento();
        registro.setUsuario(usuarioActual());
        registro.setTablaAfectada(tablaAfectada);
        registro.setRegistroId(registroId == null ? null : String.valueOf(registroId));
        registro.setOperacion(operacion.name());
        bitacoraMovimientoRepository.save(registro);
    }

    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal principal) {
            return principal.getUsuario();
        }
        return null;
    }
}
