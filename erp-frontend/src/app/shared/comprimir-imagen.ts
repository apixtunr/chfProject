const LADO_MAXIMO = 1600;
const CALIDAD_JPG = 0.8;

export async function comprimirImagen(archivo: File): Promise<File> {
  if (!archivo.type.startsWith('image/') || archivo.type === 'image/gif') {
    return archivo;
  }
  let imagen: ImageBitmap;
  try {
    imagen = await createImageBitmap(archivo);
  } catch {
    return archivo;
  }
  const escala = Math.min(1, LADO_MAXIMO / Math.max(imagen.width, imagen.height));
  if (escala === 1 && archivo.size <= 500 * 1024) {
    imagen.close();
    return archivo;
  }

  const lienzo = document.createElement('canvas');
  lienzo.width = Math.round(imagen.width * escala);
  lienzo.height = Math.round(imagen.height * escala);
  const contexto = lienzo.getContext('2d');
  if (!contexto) {
    imagen.close();
    return archivo;
  }
  contexto.fillStyle = '#fff';
  contexto.fillRect(0, 0, lienzo.width, lienzo.height);
  contexto.drawImage(imagen, 0, 0, lienzo.width, lienzo.height);
  imagen.close();

  const blob = await new Promise<Blob | null>((resolver) => lienzo.toBlob(resolver, 'image/jpeg', CALIDAD_JPG));
  if (!blob || blob.size >= archivo.size) {
    return archivo;
  }
  const nombre = archivo.name.replace(/\.[^.]+$/, '') + '.jpg';
  return new File([blob], nombre, { type: 'image/jpeg' });
}

export function tamanoLegible(bytes: number | null | undefined): string {
  if (!bytes) {
    return '';
  }
  return bytes < 1024 * 1024 ? `${Math.round(bytes / 1024)} KB` : `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}
