export interface BitacoraMovimientoResponse {
  idBitacoraMovimiento: number;
  idUsuario: number | null;
  usernameUsuario: string | null;
  tablaAfectada: string;
  registroId: string | null;
  operacion: string;
  nombreAtributo: string | null;
  valorAnterior: string | null;
  valorNuevo: string | null;
  ipOrigen: string | null;
  fechaMovimiento: string;
}

export interface BitacoraAccesoResponse {
  idBitacoraAcceso: number;
  idUsuario: number | null;
  usernameUsuario: string | null;
  accion: string;
  resultado: string | null;
  ipOrigen: string | null;
  navegador: string | null;
  sesionId: string | null;
  fechaAcceso: string;
}

export interface FiltrosBitacoraMovimiento {
  idUsuario: number | null;
  tabla: string | null;
  fechaDesde: string | null;
  fechaHasta: string | null;
}

export interface FiltrosBitacoraAcceso {
  idUsuario: number | null;
  resultado: string | null;
  fechaDesde: string | null;
  fechaHasta: string | null;
}
