package com.lacasadelchef.erp.security;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Clasificacion de cada ruta de la API. Es la unica fuente de verdad sobre quien puede
 * LEER que, y el guardia {@link AutorizacionApi} la aplica a toda peticion /api/**.
 *
 * Por que existe este archivo:
 *
 *   La configuracion decia anyRequest().authenticated(), o sea "lo que nadie protegio
 *   explicitamente, queda abierto". Con esa regla las escrituras quedaron cubiertas
 *   (alguien se acordo de anotarlas una por una) pero las 90 consultas no: cualquier
 *   usuario autenticado podia pedir /api/usuarios, /api/pagos o /api/rentabilidad
 *   aunque su rol no tuviera nada que ver. El menu no los mostraba, pero el menu corre
 *   en el navegador y no protege nada.
 *
 *   Aca se invierte: una ruta que no este clasificada NO se atiende. Agregar un
 *   controlador nuevo sin declararlo da 403, y la prueba RecursosApiTest falla en la
 *   compilacion, asi que el hueco no se puede reabrir por olvido.
 *
 * Las categorias:
 *
 *   PUBLICO      Sin sesion. Solo el login y el logout.
 *
 *   AUTENTICADO  Cualquiera que haya iniciado sesion. Son los catalogos que alimentan
 *                los desplegables de los formularios: departamentos, municipios,
 *                generos, tipos de... Un usuario de Ventas necesita leer municipios
 *                para cargar un cliente, y nunca va a tener una fila de permisos sobre
 *                municipios porque no es una pantalla, es una lista. Que la lista de
 *                municipios de Guatemala sea legible no expone nada del negocio.
 *                Ojo: esto es solo para LEER. Modificar un catalogo sigue exigiendo su
 *                permiso, que ya esta puesto con @PreAuthorize en cada controlador.
 *
 *   MODULO       Datos del negocio y de administracion. Exigen que el rol tenga su fila
 *                en rol_opcion para la pagina indicada. La fila puede tener alta, baja y
 *                modificacion en falso: eso significa "puede mirar, no puede tocar", que
 *                es como ya estan configurados COCINA sobre /api/eventos y BODEGA sobre
 *                /api/tipos-inventario. Dicho de otro modo: que la fila exista ES el
 *                permiso de consulta. No hizo falta inventar un permiso nuevo ni agregar
 *                una columna; el modelo ya lo decia, solo que nadie lo verificaba.
 */
public final class RecursosApi {

    public enum Acceso { PUBLICO, AUTENTICADO, MODULO, MODULO_FINANCIERO }

    /** Que exige una ruta: la categoria y, si es MODULO, sobre que pagina. */
    public record Regla(Acceso acceso, String paginaUrl) {

        static Regla publico() {
            return new Regla(Acceso.PUBLICO, null);
        }

        static Regla autenticado() {
            return new Regla(Acceso.AUTENTICADO, null);
        }

        static Regla modulo(String paginaUrl) {
            return new Regla(Acceso.MODULO, paginaUrl);
        }

        static Regla financiero(String paginaUrl) {
            return new Regla(Acceso.MODULO_FINANCIERO, paginaUrl);
        }
    }

    /**
     * Patron de ruta -> regla. Gana el patron mas especifico, de modo que las rutas
     * anidadas heredan de su modulo sin repetirlas todas (/api/eventos/7/detalles cae en
     * /api/eventos) salvo que tengan su propia entrada (/api/eventos/7/costos).
     *
     * Un segmento entre llaves, como {idEvento}, representa cualquier valor.
     */
    private static final Map<String, Regla> REGLAS = Map.ofEntries(
            // --- Sin sesion -----------------------------------------------------------
            Map.entry("/api/auth", Regla.publico()),

            // --- Con sesion, sin permiso especifico ------------------------------------
            // El panel de inicio ya viene recortado por rol desde el servicio: cada quien
            // recibe solo las secciones que le tocan, asi que no hay nada que filtrar aca.
            Map.entry("/api/inicio", Regla.autenticado()),

            // Catalogos: alimentan los desplegables de los formularios.
            Map.entry("/api/departamentos", Regla.autenticado()),
            Map.entry("/api/municipios", Regla.autenticado()),
            Map.entry("/api/estados", Regla.autenticado()),
            Map.entry("/api/tipos-estado", Regla.autenticado()),
            Map.entry("/api/generos", Regla.autenticado()),
            Map.entry("/api/puestos-empleado", Regla.autenticado()),
            Map.entry("/api/marcas-vehiculo", Regla.autenticado()),
            Map.entry("/api/lineas-vehiculo", Regla.autenticado()),
            Map.entry("/api/tipos-placa", Regla.autenticado()),
            Map.entry("/api/tipos-documento", Regla.autenticado()),
            Map.entry("/api/tipos-evento", Regla.autenticado()),
            Map.entry("/api/tipos-costo", Regla.autenticado()),
            Map.entry("/api/tipos-servicio", Regla.autenticado()),
            Map.entry("/api/tipos-inventario", Regla.autenticado()),
            Map.entry("/api/categorias-producto", Regla.autenticado()),
            Map.entry("/api/metodos-pago", Regla.autenticado()),
            Map.entry("/api/ubicaciones", Regla.autenticado()),

            // --- Datos del negocio -----------------------------------------------------
            Map.entry("/api/clientes", Regla.modulo("/api/clientes")),
            Map.entry("/api/cotizaciones", Regla.modulo("/api/cotizaciones")),
            Map.entry("/api/eventos", Regla.modulo("/api/eventos")),

            // Subrutas de un evento que NO son "cuando y donde" sino cuanto cuesta.
            //
            // Heredaban el permiso de Eventos, y eso alcanzaba para que Cocina y Bodega
            // —que ven la agenda para saber que se cocina y que hay que despachar— leyeran
            // el salario de cada persona asignada y los costos del evento. Se comprobo
            // contra el sistema: un usuario de Cocina recibia "Lesbia Anabella Coy Tuy,
            // Q350.00".
            //
            // Ahora las pide quien las carga (los roles que pueden dar de alta en Eventos:
            // hoy Operativo y Ventas) o quien lleva las finanzas (Rentabilidad). Ver la
            // agenda sigue sin exigir nada nuevo.
            Map.entry("/api/eventos/{idEvento}/empleados", Regla.financiero("/api/eventos")),
            Map.entry("/api/eventos/{idEvento}/costos", Regla.financiero("/api/eventos")),

            // Devuelve cotizaciones con sus montos, no eventos, y solo la usa el formulario
            // de evento nuevo para elegir de cual partir. Por eso se pide poder dar de
            // alta eventos, no tener el modulo de Cotizaciones: Operativo crea eventos sin
            // tener Cotizaciones, y exigirle ese modulo le rompia el formulario.
            Map.entry("/api/eventos/cotizaciones-disponibles", Regla.financiero("/api/eventos")),
            Map.entry("/api/pagos", Regla.modulo("/api/pagos")),
            Map.entry("/api/rentabilidad", Regla.modulo("/api/rentabilidad")),
            Map.entry("/api/inventarios", Regla.modulo("/api/inventarios")),
            Map.entry("/api/movimientos-inventario", Regla.modulo("/api/movimientos-inventario")),
            Map.entry("/api/productos", Regla.modulo("/api/productos")),
            Map.entry("/api/menus", Regla.modulo("/api/menus")),
            Map.entry("/api/platos", Regla.modulo("/api/platos")),
            Map.entry("/api/vehiculos", Regla.modulo("/api/vehiculos")),

            // --- Administracion --------------------------------------------------------
            Map.entry("/api/empleados", Regla.modulo("/api/empleados")),
            Map.entry("/api/usuarios", Regla.modulo("/api/usuarios")),
            Map.entry("/api/roles", Regla.modulo("/api/roles")),
            Map.entry("/api/opciones", Regla.modulo("/api/opciones")),
            Map.entry("/api/bitacora", Regla.modulo("/api/bitacora")),

            // La estructura del menu (modulos y vistas) solo se administra desde Roles y
            // permisos. El menu que ve cada usuario no sale de aca: viaja en la respuesta
            // del login, dentro de sus permisos.
            Map.entry("/api/modulos", Regla.modulo("/api/opciones")),
            Map.entry("/api/menu-vistas", Regla.modulo("/api/opciones"))
    );

    /**
     * La regla de una ruta, o vacio si no esta clasificada (y entonces se niega).
     *
     * Se compara segmento por segmento, no por texto: asi /api/clientes no captura a
     * /api/clientes-externos, y /api/eventos/37/costos encuentra su entrada propia en vez
     * de heredar la de /api/eventos.
     *
     * Entre varias coincidencias gana la mas especifica, que es la que declara mas
     * segmentos; a igual cantidad, la que usa menos comodines.
     */
    public static Optional<Regla> reglaDe(String ruta) {
        String normalizada = normalizar(ruta);
        if (normalizada == null) {
            return Optional.empty();
        }
        String[] pedida = segmentos(normalizada);
        return REGLAS.entrySet().stream()
                .filter(e -> coincide(segmentos(e.getKey()), pedida))
                .max(Comparator
                        .comparingInt((Map.Entry<String, Regla> e) -> segmentos(e.getKey()).length)
                        .thenComparingInt(e -> -comodines(segmentos(e.getKey()))))
                .map(Map.Entry::getValue);
    }

    /**
     * Deja la ruta en su forma real antes de compararla, o devuelve null si es sospechosa.
     *
     * Hace falta porque la ruta llega tal como la escribio el cliente, sin decodificar.
     * Escribiendo /api/eventos/7/emple%61dos, la comparacion literal no reconocia el
     * segmento "empleados", la regla especifica no coincidia y la ruta caia en la de
     * /api/eventos, que es mas permisiva: un usuario de Cocina obtenia los salarios. El
     * servidor si decodifica despues, asi que la peticion llegaba igual a su destino.
     *
     * Se decodifica varias veces para que un doble codificado (%2561) tampoco sirva, y se
     * rechaza lo que huela a recorrido de directorios. Ante la duda, null: sin regla, se
     * niega.
     *
     * Ojo con el sentido del ataque: al no coincidir la regla especifica, la ruta cae en
     * una MENOS especifica. Para casi todos los modulos eso significa no coincidir con
     * nada y quedar denegada; el riesgo real estaba en las subrutas cuya regla propia es
     * mas estricta que la de su modulo, que son justamente las de salarios y costos.
     */
    private static String normalizar(String ruta) {
        String actual = ruta;
        for (int intento = 0; intento < 3 && actual.indexOf('%') >= 0; intento++) {
            try {
                // El + se protege: en una ruta es un signo mas literal, no un espacio.
                String decodificada = URLDecoder.decode(actual.replace("+", "%2B"), StandardCharsets.UTF_8);
                if (decodificada.equals(actual)) {
                    break;
                }
                actual = decodificada;
            } catch (IllegalArgumentException codificacionInvalida) {
                return null;
            }
        }
        if (actual.indexOf('%') >= 0 || actual.contains("..") || actual.contains("\\")) {
            return null;
        }
        for (int i = 0; i < actual.length(); i++) {
            if (Character.isISOControl(actual.charAt(i))) {
                return null;
            }
        }
        // Parametros de ruta tipo /api/pagos;jsessionid=abc: se descartan antes de comparar.
        int puntoYComa = actual.indexOf(';');
        return puntoYComa >= 0 ? actual.substring(0, puntoYComa) : actual;
    }

    private static String[] segmentos(String ruta) {
        return Arrays.stream(ruta.split("/"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    /** El patron coincide si cubre el comienzo de la ruta pedida, segmento a segmento. */
    private static boolean coincide(String[] patron, String[] pedida) {
        if (patron.length > pedida.length) {
            return false;
        }
        for (int i = 0; i < patron.length; i++) {
            boolean comodin = patron[i].startsWith("{") && patron[i].endsWith("}");
            if (!comodin && !patron[i].equals(pedida[i])) {
                return false;
            }
        }
        return true;
    }

    private static int comodines(String[] patron) {
        return (int) Arrays.stream(patron).filter(s -> s.startsWith("{")).count();
    }

    /** Las rutas declaradas; la usa la prueba que verifica que no falte ninguna. */
    public static List<String> rutasDeclaradas() {
        return REGLAS.keySet().stream().sorted().toList();
    }

    private RecursosApi() {
    }
}
