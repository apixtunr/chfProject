package com.lacasadelchef.erp.administracion.rbac;

import com.lacasadelchef.erp.administracion.rbac.dto.MenuModuloResponse;
import com.lacasadelchef.erp.administracion.rbac.dto.MenuOpcionResponse;
import com.lacasadelchef.erp.entity.Modulo;
import com.lacasadelchef.erp.entity.Opcion;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.repository.OpcionRepository;
import com.lacasadelchef.erp.repository.RolOpcionRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Arma el menu lateral desde las tablas modulo, menu_vista y opcion.
 *
 * Devuelve, agrupadas por modulo y en el orden de la base, las opciones sobre las que el rol
 * tiene su fila en rol_opcion, que es el mismo criterio con que el backend deja consultar
 * cada ruta. Los modulos sin opciones permitidas no se envian. ADMINISTRADOR sigue la misma
 * regla que los demas roles.
 *
 * Como se dibuja cada opcion (su icono, su ruta en el frontend, el color del modulo) no es
 * dato del negocio y lo resuelve el frontend a partir de la pagina_url.
 */
@Service
@RequiredArgsConstructor
public class MenuLateralService {

    private final OpcionRepository opcionRepository;
    private final RolOpcionRepository rolOpcionRepository;

    @Transactional(readOnly = true)
    public List<MenuModuloResponse> menuDe(Usuario usuario) {
        Set<Integer> permitidas = rolOpcionRepository.findPermisosByRol(usuario.getRol().getIdRol()).stream()
                .map(ro -> ro.getOpcion().getIdOpcion())
                .collect(Collectors.toSet());

        Map<Modulo, List<MenuOpcionResponse>> porModulo = new LinkedHashMap<>();
        for (Opcion opcion : opcionRepository.findAllEnOrdenDeMenu()) {
            if (permitidas.contains(opcion.getIdOpcion())) {
                porModulo.computeIfAbsent(opcion.getMenuVista().getModulo(), m -> new ArrayList<>())
                        .add(new MenuOpcionResponse(opcion.getNombreOpcion(), opcion.getPaginaUrl(),
                                opcion.getMenuVista().getNombre()));
            }
        }
        return porModulo.entrySet().stream()
                .map(e -> new MenuModuloResponse(e.getKey().getNombre(), e.getValue()))
                .toList();
    }
}
