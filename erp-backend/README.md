# ERP La Casa del Chef — Backend

Sistema ERP de gestión administrativa y operativa para eventos de catering.
Spring Boot 4.1 · Java 21 · PostgreSQL 14+ · Maven · Flyway.

## Estado actual

Los 8 módulos del alcance tienen API REST completa:

| Módulo | Endpoints principales |
|---|---|
| Clientes | `/api/clientes`, `/api/ubicaciones` |
| Cotizaciones | `/api/cotizaciones` (+ versiones, detalle, PDF con OpenPDF) |
| Eventos | `/api/eventos` (+ costos, personal, vehículos, inventario; job automático de estados) |
| Inventarios | `/api/productos`, `/api/inventarios`, `/api/movimientos-inventario` (+ tipos y categorías) |
| Menús y platos | `/api/menus`, `/api/platos` |
| Pagos | `/api/pagos`, `/api/metodos-pago`, `/api/comprobantes` |
| Rentabilidad | `/api/rentabilidad/eventos`, `/api/rentabilidad/resumen` (vistas SQL) |
| Administración y seguridad | geografía, catálogos, RRHH, flota, RBAC (`modulo/menu_vista/opcion/rol_opcion`), usuarios, bitácoras |

Seguridad: login JWT (`/api/auth/login`) con BCrypt, bloqueo por intentos,
bitácora de accesos, y autorización fina por `rol_opcion` vía
`@PreAuthorize("@permisoService.tienePermiso(...)")` — falla cerrado, con
bypass para ADMINISTRADOR.

## Base de datos: Flyway

El esquema se versiona en `src/main/resources/db/migration`:

- `V1__esquema_inicial.sql` — 45 tablas, triggers, vistas, índices, semillas.
- `V2__evento_directo_sin_cotizacion.sql` — evento con cliente directo.

Las migraciones se aplican solas al arrancar la aplicación. **Regla: ningún
cambio de esquema a mano** — cada cambio es un nuevo `V<n>__descripcion.sql`.
Para una instalación desde cero basta con crear la base vacía
(`CREATE DATABASE lacasadelchef OWNER chfadmin;`) y arrancar la app.
Detalles y decisiones: `docs/CHANGELOG-configuracion.md`.

## Puesta en marcha

1. PostgreSQL con la base `lacasadelchef` (vacía o existente; Flyway se encarga).
2. Revisar `application.properties` (o definir `DB_PASSWORD` y `JWT_SECRET`).
3. `mvnw spring-boot:run` — API en `http://localhost:8080`, Swagger en
   `http://localhost:8080/swagger-ui.html`.
4. Primer usuario admin: generar hash con `util/PasswordHashGenerator` e
   insertarlo en `usuario` (ver comentario en esa clase).

## Tests

`mvnw test` — no requieren base de datos (Mockito):

- `JwtServiceTest`: generación/validación de tokens, expiración, firma ajena.
- `AuthServiceTest`: login exitoso, credenciales inválidas, incremento de
  intentos, bloqueo, usuario inactivo.
- `PermisoServiceTest`: bypass de admin, banderas CRUD, falla cerrado.
- `ClienteServiceImplTest`: CRUD del módulo de referencia con bitácora.

## Convenciones

- BD como fuente de verdad: `ddl-auto=validate` + migraciones Flyway.
- Controladores solo con DTOs (`XxxRequest` validado / `XxxResponse.desde()`).
- Services con interfaz + impl, `@Transactional` (readOnly en consultas),
  bitácora de movimientos en cada alta/baja/modificación.
- `@ManyToOne` siempre LAZY; en entidades `@Getter/@Setter`, nunca `@Data`.
- Columnas calculadas por la BD → `insertable=false, updatable=false`.
- Errores: `ResourceNotFoundException` (404) / `BusinessException` (400) →
  `GlobalExceptionHandler` responde JSON estándar (`ApiError`).

## Frontend (../erp-frontend)

Angular con login, shell por permisos, guards e interceptors. Módulos hechos:
clientes, cotizaciones, empleados, vehículos, eventos. Pendientes: inventario,
pagos, rentabilidad, menús/platos y pantallas de administración.
