package com.lacasadelchef.erp.common.audit;

import com.lacasadelchef.erp.entity.BitacoraAcceso;
import com.lacasadelchef.erp.entity.BitacoraMovimiento;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
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
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Bitacora automatica a nivel de campo. Hibernate invoca onPostUpdate cada vez que
 * escribe un UPDATE de una entidad, entregando el estado anterior y el nuevo; de ahi se
 * saca, por cada columna que realmente cambio, una fila en bitacora_movimiento con
 * nombre_atributo / valor_anterior / valor_nuevo. Los INSERT y DELETE siguen
 * registrandose desde cada service con BitacoraMovimientoService (una fila por
 * operacion, sin detalle de campos), y los UPDATE ya no se registran a mano: los cubre
 * este listener para todas las tablas de una vez.
 *
 * La fila se inserta con JDBC directo sobre la misma conexion/transaccion (doWork) y
 * no con el repositorio: durante el flush no se puede persistir una entidad nueva
 * desde un listener sin alterar la cola de acciones de Hibernate.
 */
@Slf4j
@Component
public class AuditoriaCambiosListener implements PostUpdateEventListener {

    private static final Set<Class<?>> ENTIDADES_EXCLUIDAS = Set.of(BitacoraMovimiento.class, BitacoraAcceso.class);

    /** Columnas de control que cambian solas (auditoria/login) y no aportan como "cambio de negocio". */
    private static final Set<String> PROPIEDADES_IGNORADAS = Set.of(
            "fechaCreacion", "fechaModificacion", "fechaUltimoAcceso", "tokensValidosDesde");

    /** Se registra que cambio, nunca el valor. */
    private static final Set<String> PROPIEDADES_ENMASCARADAS = Set.of("passwordHash");
    private static final String VALOR_ENMASCARADO = "********";

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

    private static String nombreLegible(Object entidad) {
        return Arrays.stream(entidad.getClass().getMethods())
                .filter(m -> m.getName().equals("getNombre") && m.getParameterCount() == 0
                        && m.getReturnType() == String.class)
                .findFirst()
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
