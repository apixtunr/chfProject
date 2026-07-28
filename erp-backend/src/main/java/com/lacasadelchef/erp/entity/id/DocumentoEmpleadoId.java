package com.lacasadelchef.erp.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class DocumentoEmpleadoId implements Serializable {

    @Column(name = "id_empleado")
    private Integer idEmpleado;

    @Column(name = "id_tipo_documento")
    private Integer idTipoDocumento;
}
