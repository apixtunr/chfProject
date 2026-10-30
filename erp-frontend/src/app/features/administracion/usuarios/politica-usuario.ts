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
