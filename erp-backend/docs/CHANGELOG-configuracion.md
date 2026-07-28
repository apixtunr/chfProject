# Registro de cambios de configuración

Bitácora de decisiones sobre la configuración del proyecto (`application.properties`,
propiedades personalizadas, etc.). El objetivo es dejar constancia del *por qué*
detrás de cada cambio, no solo del *qué*.

---

## 2026-07-17 — Migración de `application.yml` a `application.properties`

**Qué se hizo:** se reemplazó `src/main/resources/application.yml` por
`src/main/resources/application.properties`, con el mismo contenido expresado
en sintaxis `clave=valor`.

**Por qué:** preferencia del equipo por el formato `.properties`. Spring Boot
soporta ambos formatos de forma nativa e intercambiable (detecta automáticamente
el archivo presente en `src/main/resources`), por lo que el cambio no requiere
configuración adicional ni afecta el comportamiento de la aplicación.

---

## 2026-07-17 — Tipado de las propiedades personalizadas (`app.*`) con `@ConfigurationProperties`

**Contexto / problema:** tras la migración a `application.properties`, el IDE
marcaba como "unknown property" las siguientes claves:

- `app.security.jwt.secret`
- `app.security.jwt.expiration-minutes`
- `app.security.max-intentos-acceso`
- `app.cors.allowed-origins`

Esto **no era un error funcional** — la aplicación arrancaba y las propiedades
se inyectaban correctamente vía `@Value("${...}")` en `JwtService`,
`AuthService` y `SecurityConfig`. El aviso ocurre porque el IDE valida las
propiedades contra los metadatos que Spring Boot expone
(`spring-configuration-metadata.json`), y ese archivo solo describe las
propiedades estándar del framework (`spring.*`, `server.*`, `logging.*`, etc.).
Las propiedades bajo el prefijo `app.*` son propias del proyecto y no tienen
metadata asociada, por lo que el IDE no puede reconocerlas aunque estén bien
escritas y funcionando.

**Qué se hizo:**

1. Se agregó la dependencia `spring-boot-configuration-processor` (opcional,
   solo se usa en tiempo de compilación) en `pom.xml`.
2. Se creó `com.lacasadelchef.erp.config.AppProperties`, un *record* anotado
   con `@ConfigurationProperties(prefix = "app")` que modela toda la
   configuración personalizada (`security.jwt.secret`,
   `security.jwt.expiration-minutes`, `security.max-intentos-acceso`,
   `cors.allowed-origins`).
3. Se habilitó el escaneo de estas clases con `@ConfigurationPropertiesScan`
   en `ErpApplication`.
4. Se reemplazaron los usos de `@Value("${app...}")` por inyección de
   `AppProperties` en:
   - `security/JwtService.java`
   - `security/AuthService.java`
   - `security/SecurityConfig.java`

**Por qué:** con el *configuration processor* presente, Spring Boot genera
automáticamente los metadatos para las propiedades de `AppProperties` durante
la compilación, por lo que el IDE deja de marcarlas como desconocidas. Además,
centralizar estas propiedades en una sola clase tipada (en vez de `@Value`
repartidos en varios archivos) da autocompletado, validación de tipos en
tiempo de compilación y un único punto de referencia para ver qué configura
la aplicación.

**Impacto:** ninguno a nivel funcional ni de contrato — las claves en
`application.properties` no cambiaron. Solo cambia cómo se leen internamente.

---

## 2026-07-23 — Adopción de Flyway para migraciones de base de datos

**Contexto / problema:** el esquema evolucionó (ej. `evento.id_cliente` y
cotización opcional) con `ALTER TABLE` manuales en DBeaver. No existía ningún
script en el repositorio capaz de recrear la base actual: imposible montarla
en otra máquina o recuperarla, y sin historial de cambios de esquema.

**Qué se hizo:**

1. Dependencias `flyway-core` y `flyway-database-postgresql` (la versión la
   gestiona el BOM de Spring Boot).
2. Migraciones en `src/main/resources/db/migration`:
   - `V1__esquema_inicial.sql`: las 45 tablas, triggers, vistas, índices y
     semillas (equivalente al script original, sin BEGIN/COMMIT porque Flyway
     ya envuelve cada migración en una transacción).
   - `V2__evento_directo_sin_cotizacion.sql`: los cambios aplicados a mano,
     escritos de forma idempotente (`IF NOT EXISTS` / `DROP NOT NULL`) para
     que no fallen en la base de desarrollo donde ya existen.
3. Configuración en `application.properties`:
   - `spring.flyway.baseline-on-migrate=true` + `baseline-version=1`: en la
     base existente Flyway registra V1 como línea base (no la re-ejecuta) y
     aplica de V2 en adelante. En una base vacía ejecuta todo desde V1.

**Regla a partir de ahora:** ningún cambio de esquema se hace a mano. Cada
cambio = nuevo archivo `V<n>__descripcion.sql`; Flyway lo aplica al arrancar
la aplicación. `ddl-auto=validate` sigue verificando que las entidades JPA
coincidan con el resultado.
