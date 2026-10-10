import { Injectable } from '@angular/core';
import { NativeDateAdapter } from '@angular/material/core';

/**
 * Lee las fechas escritas a mano como dia/mes/año, igual que como las muestra el calendario.
 *
 * El adaptador nativo de Angular Material usa Date.parse, que las lee como mes/dia/año:
 * 31/12/2026 no la reconocia (y el filtro se descartaba sin avisar) y 05/03/2026 la tomaba
 * como 3 de mayo. Acepta "/", "-" o "." como separador y años de dos o cuatro digitos.
 * Una fecha que no existe (31/02/2026) queda como invalida, para que el campo la marque.
 */
@Injectable()
export class AdaptadorFechaEs extends NativeDateAdapter {
  override parse(valor: unknown, formato?: unknown): Date | null {
    if (typeof valor !== 'string') {
      return super.parse(valor, formato);
    }
    const texto = valor.trim();
    if (!texto) {
      return null;
    }
    const partes = /^(\d{1,2})[/.-](\d{1,2})[/.-](\d{2}|\d{4})$/.exec(texto);
    if (!partes) {
      return this.invalid();
    }
    const dia = Number(partes[1]);
    const mes = Number(partes[2]) - 1;
    const anio = partes[3].length === 2 ? 2000 + Number(partes[3]) : Number(partes[3]);
    const fecha = new Date(anio, mes, dia);
    // new Date corre los desbordes (31/02 -> 03/03): si no coincide, la fecha no existe.
    if (fecha.getFullYear() !== anio || fecha.getMonth() !== mes || fecha.getDate() !== dia) {
      return this.invalid();
    }
    return fecha;
  }
}
