package com.lacasadelchef.erp.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

/**
 * Campos de auditoria presentes en casi todas las tablas.
 * Son gestionados por la base de datos (DEFAULT NOW() y triggers), por eso se marcan
 * como no insertables / no actualizables. @Generated le indica a Hibernate que vuelva
 * a leer la columna despues del INSERT/UPDATE; sin esto, el objeto en memoria queda
 * con el valor viejo (o null) hasta la proxima consulta.
 */
@Getter
@MappedSuperclass
public abstract class Auditable {

    @Generated(event = EventType.INSERT)
    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "fecha_modificacion", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;
}
