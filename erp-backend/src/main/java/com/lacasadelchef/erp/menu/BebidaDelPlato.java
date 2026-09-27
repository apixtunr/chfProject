package com.lacasadelchef.erp.menu;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.entity.Bebida;
import com.lacasadelchef.erp.entity.Plato;

import java.util.List;
import java.util.stream.Collectors;

/**
 * La bebida viene incluida en cada plato del menu y no todos ofrecen las mismas: el lomo
 * relleno, te frio o rosa de Jamaica; los desayunos, jugo de naranja y estacion de cafe;
 * las boquitas, ninguna (ver plato_bebida). Aqui se decide que bebida queda en una linea
 * de cotizacion o de evento.
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
     * La que ya tenia la linea se respeta aunque despues se haya desactivado en el catalogo,
     * para poder seguir editando la linea sin cambiarle la bebida.
     */
    public static Bebida elegir(Plato plato, Integer idPedida, Bebida actual) {
        List<Bebida> opciones = plato.opcionesBebida();
        if (opciones.isEmpty() && (actual == null || idPedida == null)) {
            return null;
        }
        if (idPedida == null) {
            return opciones.size() == 1 ? opciones.get(0) : null;
        }
        if (actual != null && actual.getIdBebida().equals(idPedida)) {
            return actual;
        }
        return opciones.stream()
                .filter(opcion -> opcion.getIdBebida().equals(idPedida))
                .findFirst()
                .orElseThrow(() -> new BusinessException(opciones.isEmpty()
                        ? "'%s' no incluye bebida".formatted(plato.getNombrePlato())
                        : "'%s' incluye %s; elija una de esas".formatted(plato.getNombrePlato(), nombres(opciones))));
    }

    /** "Te frio o Rosa de Jamaica". */
    public static String nombres(List<Bebida> bebidas) {
        return bebidas.stream().map(Bebida::getNombreBebida).collect(Collectors.joining(" o "));
    }
}
