/**
 * Politica de nombres de usuario de la empresa (espeja PoliticaUsuario.java): primer
 * nombre, punto y primer apellido, en minusculas y sin tildes, igual que los correos.
 * "Ana Judith" + "López Cáceres" -> "ana.lopez".
 *
 * El backend es quien lo asigna; aqui solo se usa para mostrar de antemano como va a
 * quedar. Si ese usuario ya existe, el backend le agrega un numero (ana.lopez2).
 */
export function usuarioSegunPolitica(nombres: string | null | undefined, apellidos: string | null | undefined): string {
  const primera = (texto: string | null | undefined) =>
    (texto ?? '')
      .trim()
      .split(/\s+/)[0]
      .normalize('NFD')
      .replace(/\p{M}/gu, '')
      .toLowerCase()
      .replace(/[^a-z0-9]/g, '');
  const nombre = primera(nombres);
  const apellido = primera(apellidos);
  return nombre && apellido ? `${nombre}.${apellido}` : '';
}
