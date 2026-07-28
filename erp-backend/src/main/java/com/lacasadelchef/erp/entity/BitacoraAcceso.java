package com.lacasadelchef.erp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bitacora_acceso")
public class BitacoraAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bitacora_acceso")
    private Long idBitacoraAcceso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_accion", nullable = false)
    private Accion accion;

    @Column(name = "fecha_acceso", insertable = false, updatable = false)
    private LocalDateTime fechaAcceso;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @Column(name = "navegador", length = 255)
    private String navegador;

    @Column(name = "resultado", length = 50)
    private String resultado;

    @Column(name = "sesion_id", length = 100)
    private String sesionId;
}
