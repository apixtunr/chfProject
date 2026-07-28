package com.lacasadelchef.erp.entity;

import com.lacasadelchef.erp.entity.id.EventoVehiculoId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "evento_vehiculo")
public class EventoVehiculo {

    @EmbeddedId
    private EventoVehiculoId id;

    @MapsId("idEvento")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_evento")
    private Evento evento;

    @MapsId("idVehiculo")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vehiculo")
    private Vehiculo vehiculo;

    /** Conductor asignado (opcional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado")
    private Empleado empleado;

    @Column(name = "fecha_asignacion", insertable = false, updatable = false)
    private LocalDateTime fechaAsignacion;
}
