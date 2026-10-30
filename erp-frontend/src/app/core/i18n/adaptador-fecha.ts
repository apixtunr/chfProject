import { Injectable } from '@angular/core';
import { NativeDateAdapter } from '@angular/material/core';

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
    if (fecha.getFullYear() !== anio || fecha.getMonth() !== mes || fecha.getDate() !== dia) {
      return this.invalid();
    }
    return fecha;
  }
}
