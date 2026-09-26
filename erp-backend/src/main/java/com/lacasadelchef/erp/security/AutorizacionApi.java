package com.lacasadelchef.erp.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Guardia de lectura de la API. Corre antes que cualquier controlador y decide si la
 * peticion sigue, aplicando la tabla de {@link RecursosApi}.
 *
 * Reparto de responsabilidades, para que no queden dos lugares diciendo lo mismo:
 *
 *   - ESCRITURAS (POST, PUT, PATCH, DELETE): las sigue resolviendo el @PreAuthorize de
 *     cada metodo, que ya distingue alta de baja y de modificacion. De 140 escrituras,
 *     138 ya lo tenian y las 2 restantes son el login y el logout. Aca solo se comprueba
 *     que la ruta este declarada; el permiso fino lo pone la anotacion.
 *
 *   - LECTURAS (GET): las resuelve este guardia. Es lo que faltaba por completo: 90 de
 *     las 91 consultas no verificaban nada.
 *
 * Y sobre todo: una ruta que no aparezca en la tabla se niega. Esa es la diferencia con
 * la regla anterior (anyRequest().authenticated()), donde lo no declarado quedaba
 * abierto. Si manana se agrega un controlador y nadie lo clasifica, devuelve 403 en vez
 * de exponer los datos, y la prueba RecursosApiTest lo detecta antes de llegar ahi.
 */
@Component
@RequiredArgsConstructor
public class AutorizacionApi implements AuthorizationManager<RequestAuthorizationContext> {

    private static final String PAGINA_RENTABILIDAD = "/api/rentabilidad";

    /** Verbos que modifican datos; su permiso fino lo pone el @PreAuthorize de cada metodo. */
    private static final Set<String> ESCRITURAS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final PermisoService permisoService;

    @Override
    public AuthorizationDecision authorize(Supplier<? extends Authentication> autenticacion,
                                           RequestAuthorizationContext contexto) {
        String ruta = contexto.getRequest().getRequestURI();

        RecursosApi.Regla regla = RecursosApi.reglaDe(ruta).orElse(null);
        if (regla == null) {
            // Ruta no clasificada: se niega. Negar por defecto es justamente el cambio.
            return new AuthorizationDecision(false);
        }
        if (regla.acceso() == RecursosApi.Acceso.PUBLICO) {
            return new AuthorizationDecision(true);
        }

        Authentication auth = autenticacion.get();
        if (auth == null || !auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }
        if (regla.acceso() == RecursosApi.Acceso.AUTENTICADO) {
            return new AuthorizationDecision(true);
        }

        // MODULO. Las escrituras las filtra despues el @PreAuthorize del metodo, que sabe
        // si lo que se pide es un alta, una baja o una modificacion; aca alcanza con haber
        // llegado hasta este punto con la ruta declarada.
        //
        // Se listan los verbos de escritura en vez de preguntar "si no es GET, pasa". Esa
        // version dejaba entrar HEAD, y HEAD no es inocuo: Spring lo atiende con el mismo
        // metodo del @GetMapping, que no lleva @PreAuthorize. O sea que la consulta se
        // ejecutaba completa y, aunque el cuerpo no se devuelve, la respuesta ya delata si
        // hay datos y cuantos. Con la lista al reves, cualquier verbo que no sea una
        // escritura conocida —HEAD hoy, o el que se agregue manana— se trata como consulta
        // y pasa por la verificacion.
        if (ESCRITURAS.contains(contexto.getRequest().getMethod().toUpperCase(Locale.ROOT))) {
            return new AuthorizationDecision(true);
        }

        // Subrutas con dinero adentro (salarios del personal del evento, costos). Ver la
        // agenda de eventos no alcanza: se pide poder cargarlas —quien da de alta en
        // Eventos es quien fija esos montos— o llevar las finanzas.
        if (regla.acceso() == RecursosApi.Acceso.MODULO_FINANCIERO) {
            return new AuthorizationDecision(
                    permisoService.tienePermiso(regla.paginaUrl(), TipoPermiso.ALTA)
                            || permisoService.puedeConsultar(PAGINA_RENTABILIDAD));
        }

        // Consulta: el rol necesita su fila en rol_opcion para esta pagina. Que la fila
        // exista es el permiso de ver, aunque tenga alta, baja y modificacion en falso.
        return new AuthorizationDecision(permisoService.puedeConsultar(regla.paginaUrl()));
    }
}
