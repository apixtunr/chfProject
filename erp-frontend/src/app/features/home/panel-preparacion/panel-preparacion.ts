import { Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { EventoPanel } from '../dto/inicio';
import { diasHasta, hora, mesCorto } from '../inicio-formato';

interface Paso {
  etiqueta: string;
  icono: string;
  listo: boolean;
}

/**
 * Eventos en CREADO: lo que les falta para poder planificarse y cuanto tiempo queda antes
 * de que el sistema los cancele solos (ver EventoEstadoSchedulerService en el backend).
 * Los pasos son los mismos cuatro que valida EventoServiceImpl.planificar().
 */
@Component({
  selector: 'app-panel-preparacion',
  imports: [RouterLink, MatIconModule, MatTooltipModule],
  templateUrl: './panel-preparacion.html',
  styleUrl: './panel-preparacion.scss',
})
export class PanelPreparacion {
  readonly eventos = input.required<EventoPanel[]>();
  readonly total = input<number | null>(null);
  readonly hoy = input.required<string>();

  readonly hora = hora;
  readonly mesCorto = mesCorto;

  pasos(e: EventoPanel): Paso[] {
    const pasos: Paso[] = [];
    if (e.requiereMenu) pasos.push({ etiqueta: 'Menú', icono: 'restaurant_menu', listo: e.tieneMenu });
    pasos.push(
      { etiqueta: 'Personal', icono: 'groups', listo: e.tienePersonal },
      { etiqueta: 'Vehículos', icono: 'local_shipping', listo: e.tieneVehiculos },
      { etiqueta: 'Inventario', icono: 'inventory_2', listo: e.tieneInventario },
    );
    return pasos;
  }

  avance(e: EventoPanel): number {
    const pasos = this.pasos(e);
    return Math.round((pasos.filter((p) => p.listo).length / pasos.length) * 100);
  }

  dias(e: EventoPanel): number {
    return diasHasta(e.fechaEvento, this.hoy());
  }

  dia(e: EventoPanel): number {
    return Number(e.fechaEvento.substring(8, 10));
  }

  /** Cuenta regresiva hasta la hora de inicio, que es cuando se cancela si sigue sin planificar. */
  plazo(e: EventoPanel): string {
    const dias = this.dias(e);
    if (dias <= 0) return e.horaInicio ? `Se cancela hoy a las ${hora(e.horaInicio)}` : 'Se cancela hoy';
    if (dias === 1) return 'Queda 1 día';
    return `Quedan ${dias} días`;
  }
}
