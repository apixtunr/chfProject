package com.lacasadelchef.erp.entity;

import com.lacasadelchef.erp.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "opcion")
public class Opcion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opcion")
    private Integer idOpcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_menu_vista", nullable = false)
    private MenuVista menuVista;

    @Column(name = "nombre_opcion", nullable = false, length = 80)
    private String nombreOpcion;

    @Column(name = "orden_menu_vista", nullable = false)
    private Integer ordenMenuVista = 0;

    @Column(name = "pagina_url", length = 255)
    private String paginaUrl;

    @Column(name = "accion", length = 80)
    private String accion;
}
