package com.lacasadelchef.erp.security;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class RecursosApi {

    public enum Acceso { PUBLICO, AUTENTICADO, MODULO, MODULO_FINANCIERO }

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

    private static final Map<String, Regla> REGLAS = Map.ofEntries(
            Map.entry("/api/auth", Regla.publico()),

            Map.entry("/api/inicio", Regla.autenticado()),
            Map.entry("/api/menu", Regla.autenticado()),

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

            Map.entry("/api/clientes", Regla.modulo("/api/clientes")),
            Map.entry("/api/cotizaciones", Regla.modulo("/api/cotizaciones")),
            Map.entry("/api/eventos", Regla.modulo("/api/eventos")),

            Map.entry("/api/eventos/{idEvento}/empleados", Regla.financiero("/api/eventos")),
            Map.entry("/api/eventos/{idEvento}/costos", Regla.financiero("/api/eventos")),

            Map.entry("/api/eventos/cotizaciones-disponibles", Regla.financiero("/api/eventos")),
            Map.entry("/api/eventos/{idEvento}/anticipo", Regla.financiero("/api/eventos")),
            Map.entry("/api/pagos", Regla.modulo("/api/pagos")),

            Map.entry("/api/clientes/reportes/clientes", Regla.modulo("/api/clientes/reportes/clientes")),
            Map.entry("/api/cotizaciones/reportes/cotizaciones", Regla.modulo("/api/cotizaciones/reportes/cotizaciones")),
            Map.entry("/api/eventos/resumen", Regla.modulo("/api/eventos/resumen")),
            Map.entry("/api/eventos/reportes/eventos", Regla.modulo("/api/eventos/reportes/eventos")),
            Map.entry("/api/movimientos-inventario/reportes/movimientos",
                    Regla.modulo("/api/movimientos-inventario/reportes/movimientos")),
            Map.entry("/api/pagos/reportes/recibos", Regla.modulo("/api/pagos/reportes/recibos")),
            Map.entry("/api/pagos/reportes/cuentas-por-cobrar", Regla.modulo("/api/pagos/reportes/cuentas-por-cobrar")),
            Map.entry("/api/rentabilidad/reportes/pago-personal", Regla.modulo("/api/rentabilidad/reportes/pago-personal")),
            Map.entry("/api/rentabilidad", Regla.modulo("/api/rentabilidad")),
            Map.entry("/api/inventarios", Regla.modulo("/api/inventarios")),
            Map.entry("/api/movimientos-inventario", Regla.modulo("/api/movimientos-inventario")),
            Map.entry("/api/productos", Regla.modulo("/api/productos")),
            Map.entry("/api/menus", Regla.modulo("/api/menus")),
            Map.entry("/api/platos", Regla.modulo("/api/platos")),
            Map.entry("/api/bebidas", Regla.modulo("/api/bebidas")),
            Map.entry("/api/vehiculos", Regla.modulo("/api/vehiculos")),

            Map.entry("/api/empleados", Regla.modulo("/api/empleados")),
            Map.entry("/api/usuarios", Regla.modulo("/api/usuarios")),
            Map.entry("/api/roles", Regla.modulo("/api/roles")),
            Map.entry("/api/opciones", Regla.modulo("/api/opciones")),
            Map.entry("/api/bitacora", Regla.modulo("/api/bitacora")),

            Map.entry("/api/modulos", Regla.modulo("/api/opciones")),
            Map.entry("/api/menu-vistas", Regla.modulo("/api/opciones"))
    );

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

    private static String normalizar(String ruta) {
        String actual = ruta;
        for (int intento = 0; intento < 3 && actual.indexOf('%') >= 0; intento++) {
            try {
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
        int puntoYComa = actual.indexOf(';');
        return puntoYComa >= 0 ? actual.substring(0, puntoYComa) : actual;
    }

    private static String[] segmentos(String ruta) {
        return Arrays.stream(ruta.split("/"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

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

    public static List<String> rutasDeclaradas() {
        return REGLAS.keySet().stream().sorted().toList();
    }

    private RecursosApi() {
    }
}
