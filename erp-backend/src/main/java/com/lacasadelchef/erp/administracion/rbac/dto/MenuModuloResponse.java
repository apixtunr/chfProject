package com.lacasadelchef.erp.administracion.rbac.dto;

import java.util.List;

public record MenuModuloResponse(String modulo, List<MenuOpcionResponse> opciones) {
}
