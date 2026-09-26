package com.lacasadelchef.erp.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Red de seguridad del control de acceso.
 *
 * Recorre el codigo fuente de todos los controladores y verifica que cada ruta expuesta
 * este clasificada en {@link RecursosApi}. Una ruta sin clasificar se niega en tiempo de
 * ejecucion (devuelve 403), asi que el sistema no queda expuesto; lo que esta prueba
 * agrega es avisar en la compilacion, antes de que alguien descubra en la demostracion
 * que una pantalla nueva no responde.
 *
 * Esta prueba existe porque el problema original no fue una anotacion mal escrita sino un
 * olvido repetido 90 veces: mientras proteger fuera opcional, se iba a volver a olvidar.
 */
class RecursosApiTest {

    private static final Path CONTROLADORES = Path.of("src/main/java/com/lacasadelchef/erp");
    private static final Pattern RUTA_BASE = Pattern.compile("@RequestMapping\\(\\s*\"([^\"]+)\"");

    /**
     * Rutas escritas directamente en un metodo. Un controlador puede no tener
     * @RequestMapping a nivel de clase y poner la ruta completa en cada metodo; mirando
     * solo la anotacion de la clase, esas rutas pasaban desapercibidas.
     */
    private static final Pattern RUTA_EN_METODO = Pattern.compile(
            "@(?:Get|Post|Put|Patch|Delete|Request)Mapping\\(\\s*(?:value\\s*=\\s*)?\"(/[^\"]*)\"");

    private static List<String> rutasDeLosControladores() {
        try (Stream<Path> archivos = Files.walk(CONTROLADORES)) {
            return archivos
                    .filter(p -> p.getFileName().toString().endsWith("Controller.java"))
                    .flatMap(RecursosApiTest::rutasDe)
                    .distinct()
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Las rutas que expone un controlador: la de la clase si la tiene, y ademas cualquier
     * ruta absoluta escrita en un metodo. Las relativas (las que cuelgan de la base) no
     * hace falta mirarlas: heredan por prefijo.
     */
    private static Stream<String> rutasDe(Path archivo) {
        String fuente;
        try {
            fuente = Files.readString(archivo, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        Stream.Builder<String> rutas = Stream.builder();
        Matcher base = RUTA_BASE.matcher(fuente);
        boolean tieneBase = base.find();
        if (tieneBase) {
            rutas.add(base.group(1));
        }
        if (!tieneBase) {
            Matcher enMetodo = RUTA_EN_METODO.matcher(fuente);
            while (enMetodo.find()) {
                rutas.add(enMetodo.group(1));
            }
        }
        return rutas.build();
    }

    @Test
    @DisplayName("Toda ruta expuesta por un controlador esta clasificada en RecursosApi")
    void ningunaRutaSinClasificar() {
        List<String> rutas = rutasDeLosControladores();
        assertThat(rutas)
                .as("no se encontro ningun controlador; revisar la ruta de busqueda")
                .isNotEmpty();

        List<String> sinClasificar = rutas.stream()
                .filter(r -> RecursosApi.reglaDe(r).isEmpty())
                .toList();

        assertThat(sinClasificar)
                .as("""
                        Estas rutas no estan declaradas en RecursosApi y por lo tanto se niegan \
                        con 403. Agregalas a la tabla eligiendo su categoria: PUBLICO (sin sesion), \
                        AUTENTICADO (catalogo que alimenta formularios) o MODULO (datos del negocio \
                        o de administracion, exige la fila de rol_opcion).""")
                .isEmpty();
    }

    @Test
    @DisplayName("Las rutas anidadas heredan la regla de su modulo")
    void rutasAnidadasHeredan() {
        // Ojo: costos y empleados ya NO heredan, tienen regla propia por exponer montos.
        assertThat(RecursosApi.reglaDe("/api/eventos/7/detalles"))
                .get()
                .isEqualTo(RecursosApi.reglaDe("/api/eventos").orElseThrow());

        assertThat(RecursosApi.reglaDe("/api/cotizaciones/versiones/3/detalles"))
                .get()
                .isEqualTo(RecursosApi.reglaDe("/api/cotizaciones").orElseThrow());
    }

    @Test
    @DisplayName("Las subrutas con montos tienen regla propia y no heredan de Eventos")
    void subrutasConMontosNoHeredan() {
        assertThat(RecursosApi.reglaDe("/api/eventos/7/costos").orElseThrow().acceso())
                .isEqualTo(RecursosApi.Acceso.MODULO_FINANCIERO);
        assertThat(RecursosApi.reglaDe("/api/eventos/7/empleados").orElseThrow().acceso())
                .isEqualTo(RecursosApi.Acceso.MODULO_FINANCIERO);
        assertThat(RecursosApi.reglaDe("/api/eventos/cotizaciones-disponibles").orElseThrow().acceso())
                .isEqualTo(RecursosApi.Acceso.MODULO_FINANCIERO);
    }

    @Test
    @DisplayName("Gana el prefijo mas largo, no el primero que coincida")
    void ganaElPrefijoMasLargo() {
        // /api/movimientos-inventario no debe resolverse por un prefijo mas corto.
        RecursosApi.Regla regla = RecursosApi.reglaDe("/api/movimientos-inventario").orElseThrow();
        assertThat(regla.paginaUrl()).isEqualTo("/api/movimientos-inventario");
    }

    @Test
    @DisplayName("Una ruta desconocida no tiene regla, asi que se niega")
    void rutaDesconocidaSeNiega() {
        assertThat(RecursosApi.reglaDe("/api/modulo-que-no-existe")).isEmpty();
        assertThat(RecursosApi.reglaDe("/api")).isEmpty();
    }

    @Test
    @DisplayName("Un prefijo no captura a otro que solo comparte el comienzo del nombre")
    void noCapturaPorPrefijoDeNombre() {
        // /api/clientes no debe capturar a una hipotetica /api/clientes-externos.
        assertThat(RecursosApi.reglaDe("/api/clientes-externos")).isEmpty();
    }
}
