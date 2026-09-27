package com.lacasadelchef.erp.menu;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.entity.Plato;

import java.util.List;

/**
 * La bebida viene incluida en cada plato del menu y no todos ofrecen las mismas: el lomo
 * relleno, te frio o rosa de Jamaica; los desayunos, jugo de naranja y cafe; las boquitas,
 * ninguna. Aqui se decide que bebida queda en una linea de cotizacion o de evento.
 */
public final class BebidaDelPlato {

    private BebidaDelPlato() {
    }

    /**
     * La bebida a guardar para este plato:
     * <ul>
     *   <li>si el plato no incluye bebida, ninguna (se ignora lo que venga);</li>
     *   <li>si se eligio una, tiene que ser de las del plato;</li>
     *   <li>si no se eligio y el plato tiene una sola (atol, jugo y cafe), esa;</li>
     *   <li>si no se eligio y hay varias, queda pendiente: se exige al enviar la cotizacion.</li>
     * </ul>
     */
    public static String elegir(Plato plato, String pedida) {
        List<String> opciones = plato.opcionesBebida();
        if (opciones.isEmpty()) {
            return null;
        }
        if (pedida == null || pedida.isBlank()) {
            return opciones.size() == 1 ? opciones.get(0) : null;
        }
        return opciones.stream()
                .filter(opcion -> opcion.equalsIgnoreCase(pedida.trim()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("'%s' incluye %s, no %s"
                        .formatted(plato.getNombrePlato(), String.join(" o ", opciones), pedida.trim())));
    }
}
