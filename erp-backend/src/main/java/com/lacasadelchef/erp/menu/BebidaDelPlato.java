package com.lacasadelchef.erp.menu;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.entity.Bebida;
import com.lacasadelchef.erp.entity.Plato;

import java.util.List;
import java.util.stream.Collectors;

public final class BebidaDelPlato {

    private BebidaDelPlato() {
    }

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

    public static String nombres(List<Bebida> bebidas) {
        return bebidas.stream().map(Bebida::getNombreBebida).collect(Collectors.joining(" o "));
    }
}
