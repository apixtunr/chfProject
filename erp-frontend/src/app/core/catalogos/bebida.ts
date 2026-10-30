
export interface BebidaResumen {
  idBebida: number;
  nombreBebida: string;
}

export interface BebidaRequest {
  nombreBebida: string;
  idEstado: number;
}

export interface BebidaResponse {
  idBebida: number;
  nombreBebida: string;
  idEstado: number;
  estadoNombre: string;
  fechaCreacion: string;
  fechaModificacion: string | null;
}

export function nombresBebidas(bebidas: BebidaResumen[]): string {
  return bebidas.map((b) => b.nombreBebida).join(' o ');
}
