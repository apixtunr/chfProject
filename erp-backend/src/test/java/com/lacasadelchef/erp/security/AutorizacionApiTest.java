package com.lacasadelchef.erp.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Las decisiones del guardia de acceso, una por una.
 *
 * Cubre lo que hay que poder demostrar del control por rol: que un rol sin su fila en
 * rol_opcion no puede consultar el modulo, que una ruta no declarada se niega, y que HEAD
 * se verifica igual que GET.
 *
 * Lo de HEAD merece explicacion porque fue un agujero real. El guardia decia "si no es
 * GET, dejalo pasar, ya lo filtrara el @PreAuthorize del metodo". Pero Spring atiende las
 * peticiones HEAD con el mismo metodo del @GetMapping, y ese metodo no lleva
 * @PreAuthorize: un HEAD ejecutaba la consulta completa. El cuerpo no se devuelve, pero
 * la respuesta ya delata si hay datos. Se comprobo contra el sistema corriendo: un usuario
 * de Cocina recibia 403 en GET /api/pagos y 200 en HEAD /api/pagos.
 */
@ExtendWith(MockitoExtension.class)
class AutorizacionApiTest {

    @Mock private PermisoService permisoService;
    @InjectMocks private AutorizacionApi autorizacionApi;

    private static RequestAuthorizationContext peticion(String metodo, String ruta) {
        MockHttpServletRequest request = new MockHttpServletRequest(metodo, ruta);
        request.setRequestURI(ruta);
        return new RequestAuthorizationContext(request, Map.of());
    }

    private static Authentication conectado() {
        return new UsernamePasswordAuthenticationToken(
                "cocina", null, List.of(new SimpleGrantedAuthority("ROLE_COCINA")));
    }

    private boolean permite(Authentication quien, String metodo, String ruta) {
        var decision = autorizacionApi.authorize(() -> quien, peticion(metodo, ruta));
        return decision != null && decision.isGranted();
    }

    // --- Consultas de modulo ---------------------------------------------------

    @Test
    @DisplayName("Sin la fila de rol_opcion no se puede consultar el modulo")
    void consultaSinPermisoSeNiega() {
        when(permisoService.puedeConsultar("/api/pagos")).thenReturn(false);

        assertThat(permite(conectado(), "GET", "/api/pagos")).isFalse();
    }

    @Test
    @DisplayName("Con la fila de rol_opcion si se puede consultar el modulo")
    void consultaConPermisoSePermite() {
        when(permisoService.puedeConsultar("/api/pagos")).thenReturn(true);

        assertThat(permite(conectado(), "GET", "/api/pagos")).isTrue();
    }

    @Test
    @DisplayName("Las rutas anidadas se verifican contra el permiso de su modulo")
    void rutaAnidadaUsaElPermisoDelModulo() {
        when(permisoService.puedeConsultar("/api/eventos")).thenReturn(false);

        assertThat(permite(conectado(), "GET", "/api/eventos/7/detalles")).isFalse();
    }

    // --- HEAD y otros verbos ---------------------------------------------------

    @Test
    @DisplayName("HEAD se verifica igual que GET y no rodea el control")
    void headSeVerificaComoConsulta() {
        when(permisoService.puedeConsultar("/api/pagos")).thenReturn(false);

        assertThat(permite(conectado(), "HEAD", "/api/pagos")).isFalse();
    }

    @Test
    @DisplayName("Un verbo desconocido tambien se trata como consulta, no se deja pasar")
    void verboRaroSeTrataComoConsulta() {
        when(permisoService.puedeConsultar("/api/pagos")).thenReturn(false);

        assertThat(permite(conectado(), "PROPFIND", "/api/pagos")).isFalse();
    }

    @Test
    @DisplayName("Las escrituras pasan el guardia: su permiso fino lo pone el @PreAuthorize")
    void escriturasPasanAlPreAuthorize() {
        for (String metodo : List.of("POST", "PUT", "PATCH", "DELETE")) {
            assertThat(permite(conectado(), metodo, "/api/pagos"))
                    .as("el guardia deberia dejar pasar " + metodo)
                    .isTrue();
        }
        verify(permisoService, never()).puedeConsultar(anyString());
    }

    // --- Subrutas con dinero adentro --------------------------------------------

    @Test
    @DisplayName("Ver la agenda de eventos no da acceso a los salarios del personal")
    void salariosNoSalenConSoloVerEventos() {
        when(permisoService.tienePermiso("/api/eventos", TipoPermiso.ALTA)).thenReturn(false);
        when(permisoService.puedeConsultar("/api/rentabilidad")).thenReturn(false);

        assertThat(permite(conectado(), "GET", "/api/eventos/37/empleados")).isFalse();
    }

    @Test
    @DisplayName("Ver la agenda de eventos no da acceso a los costos")
    void costosNoSalenConSoloVerEventos() {
        when(permisoService.tienePermiso("/api/eventos", TipoPermiso.ALTA)).thenReturn(false);
        when(permisoService.puedeConsultar("/api/rentabilidad")).thenReturn(false);

        assertThat(permite(conectado(), "GET", "/api/eventos/37/costos")).isFalse();
    }

    @Test
    @DisplayName("Quien carga los eventos si ve salarios y costos: es quien fija esos montos")
    void quienCargaEventosVeLosMontos() {
        when(permisoService.tienePermiso("/api/eventos", TipoPermiso.ALTA)).thenReturn(true);

        assertThat(permite(conectado(), "GET", "/api/eventos/37/empleados")).isTrue();
        assertThat(permite(conectado(), "GET", "/api/eventos/37/costos")).isTrue();
    }

    @Test
    @DisplayName("Quien lleva las finanzas tambien ve salarios y costos")
    void finanzasVeLosMontos() {
        when(permisoService.tienePermiso("/api/eventos", TipoPermiso.ALTA)).thenReturn(false);
        when(permisoService.puedeConsultar("/api/rentabilidad")).thenReturn(true);

        assertThat(permite(conectado(), "GET", "/api/eventos/37/costos")).isTrue();
    }

    @Test
    @DisplayName("Las cotizaciones disponibles las ve quien puede crear eventos, no quien solo los mira")
    void cotizacionesDisponiblesSoloParaQuienCreaEventos() {
        when(permisoService.tienePermiso("/api/eventos", TipoPermiso.ALTA)).thenReturn(false);
        when(permisoService.puedeConsultar("/api/rentabilidad")).thenReturn(false);
        assertThat(permite(conectado(), "GET", "/api/eventos/cotizaciones-disponibles")).isFalse();
    }

    @Test
    @DisplayName("Quien crea eventos si ve las cotizaciones disponibles, aunque no tenga el modulo Cotizaciones")
    void quienCreaEventosVeLasCotizacionesDisponibles() {
        when(permisoService.tienePermiso("/api/eventos", TipoPermiso.ALTA)).thenReturn(true);

        assertThat(permite(conectado(), "GET", "/api/eventos/cotizaciones-disponibles")).isTrue();
    }

    @Test
    @DisplayName("Las demas subrutas de un evento siguen heredando el permiso de Eventos")
    void otrasSubrutasHeredanDeEventos() {
        when(permisoService.puedeConsultar("/api/eventos")).thenReturn(true);

        assertThat(permite(conectado(), "GET", "/api/eventos/37/detalles")).isTrue();
        assertThat(permite(conectado(), "GET", "/api/eventos/37/vehiculos")).isTrue();
    }

    // --- Rutas disfrazadas -------------------------------------------------------
    //
    // La ruta llega sin decodificar. Escribiendo emple%61dos, la comparacion literal no
    // reconocia el segmento y la ruta caia en la regla de /api/eventos, mas permisiva:
    // Cocina obtenia los salarios. Se comprobo contra el sistema corriendo (200 con el
    // salario de una persona). Ahora se decodifica antes de comparar.

    @Test
    @DisplayName("Una letra codificada no evita la regla de los salarios")
    void letraCodificadaNoEvitaLaRegla() {
        when(permisoService.tienePermiso("/api/eventos", TipoPermiso.ALTA)).thenReturn(false);
        when(permisoService.puedeConsultar("/api/rentabilidad")).thenReturn(false);

        assertThat(permite(conectado(), "GET", "/api/eventos/37/emple%61dos")).isFalse();
    }

    @Test
    @DisplayName("Tampoco sirve codificar dos veces")
    void dobleCodificacionNoEvitaLaRegla() {
        lenient().when(permisoService.tienePermiso("/api/eventos", TipoPermiso.ALTA)).thenReturn(false);
        lenient().when(permisoService.puedeConsultar(anyString())).thenReturn(false);

        assertThat(permite(conectado(), "GET", "/api/eventos/37/emple%2561dos")).isFalse();
    }

    @Test
    @DisplayName("Una ruta con recorrido de directorios se niega")
    void recorridoDeDirectoriosSeNiega() {
        lenient().when(permisoService.puedeConsultar(anyString())).thenReturn(true);

        assertThat(permite(conectado(), "GET", "/api/eventos/../pagos")).isFalse();
        assertThat(permite(conectado(), "GET", "/api/eventos/%2e%2e/pagos")).isFalse();
    }

    @Test
    @DisplayName("Un parametro de ruta pegado al final no cambia la regla que se aplica")
    void parametroDeRutaNoCambiaLaRegla() {
        when(permisoService.puedeConsultar("/api/pagos")).thenReturn(false);

        assertThat(permite(conectado(), "GET", "/api/pagos;jsessionid=abc123")).isFalse();
    }

    @Test
    @DisplayName("Una ruta codificada que no corresponde a ningun modulo tambien se niega")
    void modulosCodificadosSiguenNegados() {
        lenient().when(permisoService.puedeConsultar(anyString())).thenReturn(false);

        assertThat(permite(conectado(), "GET", "/api/p%61gos")).isFalse();
        assertThat(permite(conectado(), "GET", "/api/usu%61rios")).isFalse();
    }

    // --- Categorias de ruta ----------------------------------------------------

    @Test
    @DisplayName("Una ruta que no esta declarada en RecursosApi se niega")
    void rutaNoDeclaradaSeNiega() {
        assertThat(permite(conectado(), "GET", "/api/modulo-inventado")).isFalse();
    }

    @Test
    @DisplayName("Los catalogos se leen con solo estar conectado, sin fila de permisos")
    void catalogoNoExigePermisoDeModulo() {
        lenient().when(permisoService.puedeConsultar(anyString())).thenReturn(false);

        assertThat(permite(conectado(), "GET", "/api/departamentos")).isTrue();
        assertThat(permite(conectado(), "GET", "/api/municipios")).isTrue();
        verify(permisoService, never()).puedeConsultar(anyString());
    }

    @Test
    @DisplayName("El login es publico: no necesita sesion")
    void loginEsPublico() {
        assertThat(permite(null, "POST", "/api/auth/login")).isTrue();
    }

    // --- Sin sesion ------------------------------------------------------------

    @Test
    @DisplayName("Sin autenticacion no se permite nada que no sea publico")
    void sinSesionSeNiega() {
        assertThat(permite(null, "GET", "/api/pagos")).isFalse();
        assertThat(permite(null, "GET", "/api/departamentos")).isFalse();
    }

    @Test
    @DisplayName("Un usuario anonimo no cuenta como conectado")
    void anonimoSeNiega() {
        Authentication anonimo = new AnonymousAuthenticationToken(
                "clave", "anonimo", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        anonimo.setAuthenticated(false);

        assertThat(permite(anonimo, "GET", "/api/departamentos")).isFalse();
    }
}
