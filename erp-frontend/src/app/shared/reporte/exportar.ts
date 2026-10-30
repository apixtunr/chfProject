import { HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable } from 'rxjs';
import { VisorArchivo } from '../visor-archivo/visor-archivo';

export function aFechaIso(fecha: Date | null): string | null {
  if (!fecha) {
    return null;
  }
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${mes}-${dia}`;
}

export function parametros(valores: Record<string, string | number | null>): HttpParams {
  let params = new HttpParams();
  for (const [clave, valor] of Object.entries(valores)) {
    if (valor !== null && valor !== '') {
      params = params.set(clave, valor);
    }
  }
  return params;
}

export function mesActual(): { desde: Date; hasta: Date } {
  const hoy = new Date();
  return {
    desde: new Date(hoy.getFullYear(), hoy.getMonth(), 1),
    hasta: new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0),
  };
}

export function descargarCsv(nombre: string, encabezado: string[], filas: (string | number | null)[][]): void {
  const celda = (valor: string | number | null) =>
    typeof valor === 'number' ? String(valor) : `"${(valor ?? '').replaceAll('"', '""')}"`;
  const lineas = [encabezado.map(celda).join(','), ...filas.map((f) => f.map(celda).join(','))];
  const blob = new Blob(['﻿' + lineas.join('\n')], { type: 'text/csv;charset=utf-8' });
  const enlace = document.createElement('a');
  enlace.href = URL.createObjectURL(blob);
  enlace.download = `${nombre}_${new Date().toISOString().slice(0, 10)}.csv`;
  enlace.click();
  URL.revokeObjectURL(enlace.href);
}

@Injectable({ providedIn: 'root' })
export class VerPdf {
  private readonly dialog = inject(MatDialog);

  abrir(pdf: Observable<Blob>, titulo: string, nombre: string): void {
    pdf.subscribe((archivo) =>
      this.dialog.open(VisorArchivo, {
        data: { titulo, nombre: `${nombre}.pdf`, archivo },
        maxWidth: '95vw',
        autoFocus: false,
      }),
    );
  }
}
