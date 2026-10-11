package com.lacasadelchef.erp.administracion.rbac.dto;

/**
 * Una opcion del menu: su nombre, su pagina_url (con la que el frontend sabe a que pantalla
 * lleva) y la vista a la que pertenece.
 */
public record MenuOpcionResponse(String nombre, String paginaUrl, String vista) {
}
