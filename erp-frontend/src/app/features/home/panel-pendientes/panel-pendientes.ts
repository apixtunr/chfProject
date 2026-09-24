import { DecimalPipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { MovimientoReciente, PanelInicio } from '../dto/inicio';
import { diasHasta, fechaCorta, haceCuanto, iniciales, porcentaje } from '../inicio-formato';

const OPERACIONES: Record<string, string> = { INSERT: 'creó', UPDATE: 'modificó', DELETE: 'eliminó' };

/**
 * Columna lateral del inicio: lo que pide accion en cada area. Cada tarjeta aparece solo
 * si el backend mando esa seccion (no null), es decir, si el rol tiene acceso a la
 * pantalla de origen.
 */
@Component({
  selector: 'app-panel-pendientes',
  imports: [RouterLink, MatIconModule, MatTooltipModule, DecimalPipe],
  templateUrl: './panel-pendientes.html',
  styleUrl: './panel-pendientes.scss',
})
export class PanelPendientes {
  readonly panel = input.required<PanelInicio>();

  readonly fechaCorta = fechaCorta;
  readonly haceCuanto = haceCuanto;
  readonly iniciales = iniciales;
  readonly porcentaje = porcentaje;

  yaPaso(fecha: string): boolean {
    return diasHasta(fecha, this.panel().fechaReferencia) < 0;
  }

  esperando(fechaHora: string | null): string {
    if (!fechaHora) return '';
    const dias = Math.floor((Date.now() - new Date(fechaHora).getTime()) / 86_400_000);
    if (dias <= 0) return 'enviada hoy';
    return dias === 1 ? 'hace 1 día' : `hace ${dias} días`;
  }

  movimiento(m: MovimientoReciente): string {
    const accion = OPERACIONES[m.operacion] ?? m.operacion.toLowerCase();
    return `${accion} ${m.tabla.replace(/_/g, ' ')} #${m.registroId}`;
  }
}
