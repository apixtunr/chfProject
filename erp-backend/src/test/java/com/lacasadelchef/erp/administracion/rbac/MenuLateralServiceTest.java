package com.lacasadelchef.erp.administracion.rbac;

import com.lacasadelchef.erp.administracion.rbac.dto.MenuModuloResponse;
import com.lacasadelchef.erp.administracion.rbac.dto.MenuOpcionResponse;
import com.lacasadelchef.erp.entity.MenuVista;
import com.lacasadelchef.erp.entity.Modulo;
import com.lacasadelchef.erp.entity.Opcion;
import com.lacasadelchef.erp.entity.Rol;
import com.lacasadelchef.erp.entity.RolOpcion;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.repository.OpcionRepository;
import com.lacasadelchef.erp.repository.RolOpcionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuLateralServiceTest {

    @Mock private OpcionRepository opcionRepository;
    @Mock private RolOpcionRepository rolOpcionRepository;
    @InjectMocks private MenuLateralService menuLateralService;

    private Opcion pagos;
    private Opcion recibos;
    private Opcion departamentos;

    @BeforeEach
    void armarMenu() {
        Modulo moduloPagos = modulo("Pagos");
        Modulo administracion = modulo("Administración");
        MenuVista vistaPagos = vista(moduloPagos, "Pagos");
        MenuVista catalogos = vista(administracion, "Catálogos");

        pagos = opcion(1, vistaPagos, "Pagos", "/api/pagos");
        recibos = opcion(2, vistaPagos, "Recibos emitidos", "/api/pagos/reportes/recibos");
        departamentos = opcion(3, catalogos, "Departamentos", "/api/departamentos");

        when(opcionRepository.findAllEnOrdenDeMenu()).thenReturn(List.of(pagos, recibos, departamentos));
    }

    @Test
    @DisplayName("ADMINISTRADOR recibe todas las opciones, agrupadas por modulo y en orden")
    void administradorVeTodo() {
        List<MenuModuloResponse> menu = menuLateralService.menuDe(usuario("ADMINISTRADOR", 1));

        assertThat(menu).extracting(MenuModuloResponse::modulo).containsExactly("Pagos", "Administración");
        assertThat(menu.get(0).opciones()).containsExactly(
                new MenuOpcionResponse("Pagos", "/api/pagos", "Pagos"),
                new MenuOpcionResponse("Recibos emitidos", "/api/pagos/reportes/recibos", "Pagos"));
        assertThat(menu.get(1).opciones())
                .containsExactly(new MenuOpcionResponse("Departamentos", "/api/departamentos", "Catálogos"));
        verifyNoInteractions(rolOpcionRepository);
    }

    @Test
    @DisplayName("Otro rol recibe solo sus opciones, y el modulo sin opciones permitidas no se envia")
    void rolVeSoloSusOpciones() {
        when(rolOpcionRepository.findPermisosByRol(2)).thenReturn(List.of(permiso(recibos)));

        List<MenuModuloResponse> menu = menuLateralService.menuDe(usuario("FINANZAS", 2));

        assertThat(menu).hasSize(1);
        assertThat(menu.get(0).opciones()).extracting(MenuOpcionResponse::nombre).containsExactly("Recibos emitidos");
    }

    private static Modulo modulo(String nombre) {
        Modulo m = new Modulo();
        m.setNombre(nombre);
        return m;
    }

    private static MenuVista vista(Modulo modulo, String nombre) {
        MenuVista v = new MenuVista();
        v.setModulo(modulo);
        v.setNombre(nombre);
        return v;
    }

    private static Opcion opcion(int id, MenuVista vista, String nombre, String paginaUrl) {
        Opcion o = new Opcion();
        o.setIdOpcion(id);
        o.setMenuVista(vista);
        o.setNombreOpcion(nombre);
        o.setPaginaUrl(paginaUrl);
        return o;
    }

    private static RolOpcion permiso(Opcion opcion) {
        RolOpcion ro = new RolOpcion();
        ro.setOpcion(opcion);
        return ro;
    }

    private static Usuario usuario(String nombreRol, int idRol) {
        Rol rol = new Rol();
        rol.setIdRol(idRol);
        rol.setNombreRol(nombreRol);
        Usuario u = new Usuario();
        u.setRol(rol);
        return u;
    }
}
