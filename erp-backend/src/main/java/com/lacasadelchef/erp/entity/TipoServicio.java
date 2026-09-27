package com.lacasadelchef.erp.entity;

import com.lacasadelchef.erp.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Catalogo de servicios extra que se cobran aparte del menu (hora extra de cocinero, cubremantel...). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tipo_servicio")
public class TipoServicio extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_servicio")
    private Integer idTipoServicio;

    @Column(name = "nombre_tipo", nullable = false, length = 80)
    private String nombreTipo;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    /** Precio por unidad (ej. por cocinero y hora); null = el monto se escribe en cada cotizacion. */
    @Column(name = "precio_unitario", precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    /** false = ya no se ofrece al cotizar; se conserva por las cotizaciones que lo usan. */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
