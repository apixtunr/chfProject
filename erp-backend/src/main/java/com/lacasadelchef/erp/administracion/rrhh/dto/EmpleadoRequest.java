package com.lacasadelchef.erp.administracion.rrhh.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record EmpleadoRequest(

        @NotNull(message = "El puesto es obligatorio")
        Integer idPuestoEmpleado,

        @NotNull(message = "El estado es obligatorio")
        Integer idEstado,

        Integer idGenero,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
        String apellido,

        @Email(message = "El correo no tiene un formato valido")
        @Size(max = 150)
        String correo,

        @Pattern(regexp = "^[0-9+\\- ]{8,20}$", message = "El telefono no tiene un formato valido")
        String telefono,

        LocalDate fechaContratacion,

        /** Obligatorio. Se aceptan los espacios del documento fisico ("2547 12345 0101"). */
        @NotBlank(message = "El DPI es obligatorio")
        @Pattern(regexp = "^[0-9][0-9 \\-]{11,16}[0-9]$", message = "El DPI debe tener 13 dígitos")
        String dpi,

        /**
         * Los demas documentos (licencia de conducir, etc.). La lista reemplaza a los que
         * tenga registrados: los que no vengan se quitan. Si viene null no se tocan.
         */
        @Valid
        List<DocumentoItemRequest> documentos,

        /**
         * Acceso al sistema para esta persona. Opcional y solo se toma en cuenta al dar
         * de alta: de los empleados actuales, la mayoria no entra al sistema.
         *
         * Al editar un empleado este campo tiene que venir vacio; el acceso de alguien
         * que ya existe se administra desde la pantalla de Usuarios.
         */
        @Valid
        AccesoSistemaRequest acceso
) {
}
