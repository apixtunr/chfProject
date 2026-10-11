package com.lacasadelchef.erp.administracion.rbac.dto;

import java.util.List;

/** Un modulo del menu lateral con las opciones que el usuario puede ver, en orden. */
public record MenuModuloResponse(String modulo, List<MenuOpcionResponse> opciones) {
}
