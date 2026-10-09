/** Lado mayor con el que una boleta fotografiada se sigue leyendo bien. */
const LADO_MAXIMO = 1600;
const CALIDAD_JPG = 0.8;

/**
 * Reduce una foto antes de subirla. La foto de una boleta tomada con el celular pesa
 * 3-5 MB; a 1600 px de lado y en JPG queda en 200-400 KB y se sigue leyendo bien.
 *
 * Los PDF y las imagenes que ya son chicas se devuelven tal cual. Si el navegador no
 * logra leer la imagen (formato raro), tambien se devuelve la original y el backend
 * decide si la acepta.
 */
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
  // Fondo blanco: un PNG con transparencia quedaria negro al pasarlo a JPG.
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

/** 245760 -> "240 KB", 3145728 -> "3.0 MB". */
export function tamanoLegible(bytes: number | null | undefined): string {
  if (!bytes) {
    return '';
  }
  return bytes < 1024 * 1024 ? `${Math.round(bytes / 1024)} KB` : `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}
