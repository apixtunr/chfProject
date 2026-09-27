package com.lacasadelchef.erp.bebida.dto;

import com.lacasadelchef.erp.entity.Bebida;

import java.util.List;

/** Lo minimo de una bebida para mostrarla o elegirla (en un plato, en una linea). */
public record BebidaResumen(Integer idBebida, String nombreBebida) {

    public static BebidaResumen desde(Bebida bebida) {
        return bebida == null ? null : new BebidaResumen(bebida.getIdBebida(), bebida.getNombreBebida());
    }

    public static List<BebidaResumen> lista(List<Bebida> bebidas) {
        return bebidas.stream().map(BebidaResumen::desde).toList();
    }
}
