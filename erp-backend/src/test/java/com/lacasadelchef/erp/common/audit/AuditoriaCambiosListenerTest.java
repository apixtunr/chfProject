package com.lacasadelchef.erp.common.audit;

import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.Usuario;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.jdbc.Work;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Altas y bajas dejan en la bitacora una fila con todo el registro resumido. Se usa
 * Usuario porque tiene los tres casos especiales: una columna de control que no se
 * registra, la contrasena que se enmascara y una columna que llena la base de datos.
 */
@ExtendWith(MockitoExtension.class)
class AuditoriaCambiosListenerTest {

    private static final String[] PROPIEDADES = {"username", "passwordHash", "fechaUltimoAcceso", "intentosAcceso"};
    private static final String[] COLUMNAS = {"username", "password_hash", "fecha_ultimo_acceso", "intentos_acceso"};
    /** intentos_acceso la pone la base de datos (DEFAULT 0): no se conoce al insertar. */
    private static final boolean[] INSERTABLES = {true, true, true, false};

    @Mock private EntityPersister persister;
    @Mock private EventSource session;
    @Mock private Type tipoSimple;
    @Mock private Type tipoRelacion;
    @Mock private Connection conexion;
    @Mock private PreparedStatement sentencia;

    private final AuditoriaCambiosListener listener = new AuditoriaCambiosListener();
    /** Parametros enviados al INSERT de bitacora_movimiento, por posicion (1 = id_usuario). */
    private final Map<Integer, String> parametros = new HashMap<>();

    @BeforeEach
    void preparar() throws Exception {
        lenient().when(persister.getMappedClass()).thenReturn((Class) Usuario.class);
        when(persister.getPropertyNames()).thenReturn(PROPIEDADES);
        when(persister.getPropertyTypes()).thenReturn(new Type[]{tipoSimple, tipoSimple, tipoSimple, tipoSimple});
        lenient().when(persister.getPropertyInsertability()).thenReturn(INSERTABLES);
        for (int i = 0; i < PROPIEDADES.length; i++) {
            lenient().when(persister.getPropertyColumnNames(PROPIEDADES[i])).thenReturn(new String[]{COLUMNAS[i]});
        }
        when(conexion.prepareStatement(anyString())).thenReturn(sentencia);
        doAnswer(inv -> {
            parametros.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(sentencia).setString(anyInt(), any());
        doAnswer(inv -> {
            inv.<Work>getArgument(0).execute(conexion);
            return null;
        }).when(session).doWork(any(Work.class));
    }

    @Test
    @DisplayName("Alta: una fila con todo el registro en valor_nuevo, sin columnas de control ni las que llena la base")
    void altaResumeElRegistro() {
        Object[] estado = {"vendedor1", "$2a$10$hash", null, null};

        listener.onPostInsert(new PostInsertEvent(new Usuario(), 7, estado, persister, session));

        assertThat(parametros.get(2)).isEqualTo("usuario");
        assertThat(parametros.get(3)).isEqualTo("7");
        assertThat(parametros.get(4)).isNull();
        assertThat(parametros.get(5)).isNull();
        assertThat(parametros.get(6)).isEqualTo("username: vendedor1 · password_hash: ********");
        assertThat(parametros.get(7)).isEqualTo("INSERT");
    }

    @Test
    @DisplayName("Baja: una fila con lo que tenia el registro en valor_anterior, contrasena enmascarada")
    void bajaGuardaLoQueTenia() {
        Object[] estado = {"vendedor1", "$2a$10$hash", null, 3};

        listener.onPostDelete(new PostDeleteEvent(new Usuario(), 7, estado, persister, session));

        assertThat(parametros.get(5)).isEqualTo("username: vendedor1 · password_hash: ******** · intentos_acceso: 3");
        assertThat(parametros.get(6)).isNull();
        assertThat(parametros.get(7)).isEqualTo("DELETE");
    }

    @Test
    @DisplayName("Una persona relacionada se identifica por nombre y apellido, no solo por el nombre")
    void relacionConEmpleado() {
        Empleado empleado = new Empleado();
        empleado.setNombre("Lucia");
        empleado.setApellido("Ramirez");
        when(tipoRelacion.isEntityType()).thenReturn(true);
        when(session.getContextEntityIdentifier(empleado)).thenReturn(2);
        when(persister.getPropertyNames()).thenReturn(new String[]{"username"});
        when(persister.getPropertyTypes()).thenReturn(new Type[]{tipoRelacion});

        listener.onPostDelete(new PostDeleteEvent(new Usuario(), 7, new Object[]{empleado}, persister, session));

        // Se reusa la columna "username" del persister: lo que importa es como se escribe el valor.
        assertThat(parametros.get(5)).isEqualTo("username: 2 (Lucia Ramirez)");
    }

    @Test
    @DisplayName("Un usuario relacionado se identifica por la persona y su nombre de usuario")
    void relacionConUsuario() {
        Empleado empleado = new Empleado();
        empleado.setNombre("Lucia");
        empleado.setApellido("Ramirez");
        Usuario usuario = new Usuario();
        usuario.setEmpleado(empleado);
        usuario.setUsername("lramirez");
        when(tipoRelacion.isEntityType()).thenReturn(true);
        when(session.getContextEntityIdentifier(usuario)).thenReturn(5);
        when(persister.getPropertyNames()).thenReturn(new String[]{"username"});
        when(persister.getPropertyTypes()).thenReturn(new Type[]{tipoRelacion});

        listener.onPostDelete(new PostDeleteEvent(new Usuario(), 7, new Object[]{usuario}, persister, session));

        // Se reusa la columna "username" del persister: lo que importa es como se escribe el valor.
        assertThat(parametros.get(5)).isEqualTo("username: 5 (Lucia Ramirez - lramirez)");
    }
}
