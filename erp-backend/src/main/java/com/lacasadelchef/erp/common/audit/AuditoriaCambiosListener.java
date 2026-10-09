package com.lacasadelchef.erp.common.audit;

import com.lacasadelchef.erp.entity.BitacoraAcceso;
import com.lacasadelchef.erp.entity.BitacoraMovimiento;
import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.Usuario;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Bitacora automatica de todas las tablas: Hibernate avisa cada vez que escribe un
 * INSERT, UPDATE o DELETE de una entidad, y aqui se deja constancia en
 * bitacora_movimiento sin que ningun service tenga que acordarse de hacerlo.
 * <ul>
 *   <li>UPDATE: una fila por cada columna que realmente cambio, con nombre_atributo,
 *       valor_anterior y valor_nuevo, para poder buscar quien cambio un campo dado.</li>
 *   <li>INSERT: una sola fila con todos los datos con los que nacio el registro en
 *       valor_nuevo ("cantidad_platos: 150 · id_plato: 3 (Pastel)...").</li>
 *   <li>DELETE: una sola fila con lo que tenia el registro en valor_anterior; es la
 *       unica constancia que queda de el.</li>
 * </ul>
 *
 * La fila se inserta con JDBC directo sobre la misma conexion/transaccion (doWork) y
 * no con el repositorio: durante el flush no se puede persistir una entidad nueva
 * desde un listener sin alterar la cola de acciones de Hibernate.
 */
@Slf4j
@Component
public class AuditoriaCambiosListener
        implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

    private static final Set<Class<?>> ENTIDADES_EXCLUIDAS = Set.of(BitacoraMovimiento.class, BitacoraAcceso.class);

    /** Columnas de control que cambian solas (auditoria/login) y no aportan como "cambio de negocio". */
    private static final Set<String> PROPIEDADES_IGNORADAS = Set.of(
            "fechaCreacion", "fechaModificacion", "fechaUltimoAcceso", "tokensValidosDesde");

    /**
     * Se registra que cambio, nunca el valor: la contrasena por seguridad, y el archivo de un
     * comprobante porque son cientos de KB que no se leen en una bitacora (su nombre y
     * tamano si quedan registrados).
     */
    private static final Set<String> PROPIEDADES_ENMASCARADAS = Set.of("passwordHash", "contenido");
    private static final String VALOR_ENMASCARADO = "********";

    /** Separa los campos en el resumen de un INSERT o DELETE. */
    private static final String SEPARADOR_RESUMEN = " · ";
    private static final String SIN_VALOR = "-";

    private static final String SQL_INSERT = """
            INSERT INTO bitacora_movimiento
              (id_usuario, tabla_afectada, registro_id, nombre_atributo, valor_anterior, valor_nuevo, operacion, ip_origen)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        EntityPersister persister = event.getPersister();
        Class<?> clase = persister.getMappedClass();
        if (ENTIDADES_EXCLUIDAS.contains(clase)) {
            return;
        }
        int[] dirty = event.getDirtyProperties();
        Object[] anterior = event.getOldState();
        Object[] nuevo = event.getState();
        if (dirty == null || dirty.length == 0 || anterior == null || nuevo == null) {
            return;
        }

        String[] nombres = persister.getPropertyNames();
        Type[] tipos = persister.getPropertyTypes();
        SharedSessionContractImplementor session = event.getSession();
        String tabla = nombreTabla(clase, persister);
        String registro = renderId(event.getId());
        Integer idUsuario = ContextoAuditoria.idUsuarioActual();
        String ip = ContextoAuditoria.ipActual();

        List<Object[]> filas = new ArrayList<>();
        for (int i : dirty) {
            String propiedad = nombres[i];
            if (PROPIEDADES_IGNORADAS.contains(propiedad) || tipos[i].isCollectionType()) {
                continue;
            }
            String valorAnterior;
            String valorNuevo;
            if (PROPIEDADES_ENMASCARADAS.contains(propiedad)) {
                valorAnterior = VALOR_ENMASCARADO;
                valorNuevo = VALOR_ENMASCARADO;
            } else {
                valorAnterior = render(anterior[i], tipos[i], session);
                valorNuevo = render(nuevo[i], tipos[i], session);
                if (Objects.equals(valorAnterior, valorNuevo)) {
                    continue;
                }
            }
            filas.add(new Object[]{
                    idUsuario, tabla, registro, nombreColumna(persister, propiedad),
                    valorAnterior, valorNuevo, Operacion.UPDATE.name(), ip});
        }
        if (filas.isEmpty()) {
            return;
        }

        insertar(session, filas);
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        EntityPersister persister = event.getPersister();
        if (ENTIDADES_EXCLUIDAS.contains(persister.getMappedClass())) {
            return;
        }
        SharedSessionContractImplementor session = event.getSession();
        String resumen = resumir(persister, event.getState(), session, true);
        insertar(session, List.<Object[]>of(fila(persister, event.getId(), null, resumen, Operacion.INSERT)));
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        EntityPersister persister = event.getPersister();
        if (ENTIDADES_EXCLUIDAS.contains(persister.getMappedClass())) {
            return;
        }
        SharedSessionContractImplementor session = event.getSession();
        String resumen = resumir(persister, event.getDeletedState(), session, false);
        insertar(session, List.<Object[]>of(fila(persister, event.getId(), resumen, null, Operacion.DELETE)));
    }

    /** Fila de INSERT o DELETE: todo el registro resumido, sin nombre_atributo. */
    private static Object[] fila(EntityPersister persister, Object id, String anterior, String nuevo, Operacion operacion) {
        return new Object[]{
                ContextoAuditoria.idUsuarioActual(), nombreTabla(persister.getMappedClass(), persister), renderId(id),
                null, anterior, nuevo, operacion.name(), ContextoAuditoria.ipActual()};
    }

    /**
     * Todos los campos del registro en una linea, "columna: valor", con los mismos
     * criterios que un UPDATE: sin columnas de control ni colecciones, y la contrasena
     * enmascarada. En un alta se omiten las columnas que llena la base de datos (total
     * por trigger, subtotal calculado, fecha por defecto): en ese momento Hibernate aun
     * no las conoce y saldrian vacias, como si no tuvieran valor.
     */
    private static String resumir(EntityPersister persister, Object[] estado, SharedSessionContractImplementor session,
                                  boolean esAlta) {
        if (estado == null) {
            return null;
        }
        String[] nombres = persister.getPropertyNames();
        Type[] tipos = persister.getPropertyTypes();
        boolean[] insertables = persister.getPropertyInsertability();
        List<String> partes = new ArrayList<>();
        for (int i = 0; i < nombres.length; i++) {
            String propiedad = nombres[i];
            // Lo no insertable lo calcula la base (subtotal, monto_total) y en el alta aun no
            // tiene valor. Las relaciones de una llave compuesta (@MapsId: plato y bebida en
            // plato_bebida) tambien figuran como no insertables, porque las escribe la llave,
            // pero si tienen valor y son justo lo que dice que se agrego.
            boolean calculadaPorLaBase = !insertables[i] && !tipos[i].isEntityType();
            if (PROPIEDADES_IGNORADAS.contains(propiedad) || tipos[i].isCollectionType() || (esAlta && calculadaPorLaBase)) {
                continue;
            }
            String valor = PROPIEDADES_ENMASCARADAS.contains(propiedad)
                    ? VALOR_ENMASCARADO
                    : render(estado[i], tipos[i], session);
            partes.add(nombreColumna(persister, propiedad) + ": " + (valor == null ? SIN_VALOR : valor));
        }
        return String.join(SEPARADOR_RESUMEN, partes);
    }

    private static void insertar(SharedSessionContractImplementor session, List<Object[]> filas) {
        session.doWork(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
                for (Object[] f : filas) {
                    ps.setObject(1, f[0], Types.INTEGER);
                    for (int c = 1; c < f.length; c++) {
                        ps.setString(c + 1, (String) f[c]);
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        });
    }

    private static String nombreTabla(Class<?> clase, EntityPersister persister) {
        Table tabla = clase.getAnnotation(Table.class);
        return tabla != null && !tabla.name().isBlank() ? tabla.name() : persister.getEntityName();
    }

    /** Nombre real de la columna (id_estado, hora_inicio...), no el del campo Java. */
    private static String nombreColumna(EntityPersister persister, String propiedad) {
        try {
            String[] columnas = persister.getPropertyColumnNames(propiedad);
            if (columnas != null && columnas.length > 0 && columnas[0] != null) {
                return columnas[0];
            }
        } catch (RuntimeException e) {
            log.debug("Sin nombre de columna para {}.{}: {}", persister.getEntityName(), propiedad, e.getMessage());
        }
        return propiedad.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }

    /** Las llaves compuestas (evento_inventario, menu_plato...) se muestran como "35-9", igual que los service. */
    private static String renderId(Object id) {
        if (id == null) {
            return null;
        }
        if (!id.getClass().isAnnotationPresent(Embeddable.class)) {
            return String.valueOf(id);
        }
        List<String> partes = new ArrayList<>();
        for (Field campo : id.getClass().getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(campo.getModifiers())) {
                continue;
            }
            try {
                campo.setAccessible(true);
                partes.add(String.valueOf(campo.get(id)));
            } catch (ReflectiveOperationException e) {
                partes.add("?");
            }
        }
        return String.join("-", partes);
    }

    private static String render(Object valor, Type tipo, SharedSessionContractImplementor session) {
        if (valor == null) {
            return null;
        }
        if (!tipo.isEntityType()) {
            // Sin la normalizacion, 10 y 10.00 (mismo numero, distinta escala: uno viene del
            // request y el otro de la base) se verian como un cambio que en realidad no ocurrio.
            if (valor instanceof BigDecimal numero) {
                return numero.stripTrailingZeros().toPlainString();
            }
            return String.valueOf(valor);
        }
        // Relacion (@ManyToOne): se guarda el id de la fila referida y, si la entidad tiene
        // un getNombre(), tambien ese nombre para que la bitacora se lea sin ir a buscar el id.
        try {
            Object id = session.getContextEntityIdentifier(valor);
            if (id == null) {
                id = session.getEntityPersister(null, valor).getIdentifier(valor, session);
            }
            String texto = String.valueOf(id);
            String nombre = nombreLegible(valor);
            return nombre != null ? texto + " (" + nombre + ")" : texto;
        } catch (RuntimeException e) {
            return String.valueOf(valor);
        }
    }

    /**
     * Busca el nombre de la entidad relacionada. Unas lo llaman getNombre() y otras
     * repiten la entidad en el getter (getNombreMunicipio, getNombreProducto), por eso
     * se acepta cualquiera que empiece asi, y se ordena por nombre para que la eleccion
     * no dependa del orden de reflexion. Dos casos van primero: una persona se
     * identifica por nombre y apellido (getNombreCompleto), y un usuario, que no tiene
     * nombre propio, por la persona que hay detras y su nombre de usuario.
     */
    private static String nombreLegible(Object entidad) {
        if (entidad instanceof Usuario usuario) {
            return usuario.getEmpleado().getNombreCompleto() + " - " + usuario.getUsername();
        }
        if (entidad instanceof Empleado empleado) {
            return empleado.getNombreCompleto();
        }
        return Arrays.stream(entidad.getClass().getMethods())
                .filter(m -> m.getName().startsWith("getNombre") && m.getParameterCount() == 0
                        && m.getReturnType() == String.class)
                .min(Comparator.comparing(Method::getName))
                .map(m -> invocar(m, entidad))
                .orElse(null);
    }

    private static String invocar(Method metodo, Object objetivo) {
        try {
            return (String) metodo.invoke(objetivo);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
