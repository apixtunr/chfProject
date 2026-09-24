import { Component, computed, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { EventoPanel } from '../dto/inicio';
import { fechaCorta, hora, nombreDia } from '../inicio-formato';

interface DiaAgenda {
  fecha: string;
  titulo: string;
  subtitulo: string;
  eventos: EventoPanel[];
}

/** Eventos vigentes de los proximos 7 dias, agrupados por dia. */
@Component({
  selector: 'app-panel-agenda',
  imports: [RouterLink, MatIconModule],
  templateUrl: './panel-agenda.html',
  styleUrl: './panel-agenda.scss',
})
export class PanelAgenda {
  readonly eventos = input.required<EventoPanel[]>();
  readonly hoy = input.required<string>();

  readonly hora = hora;

  readonly dias = computed<DiaAgenda[]>(() => {
    const dias = new Map<string, DiaAgenda>();
    for (const e of this.eventos()) {
      let dia = dias.get(e.fechaEvento);
      if (!dia) {
        dia = { fecha: e.fechaEvento, titulo: nombreDia(e.fechaEvento, this.hoy()), subtitulo: fechaCorta(e.fechaEvento), eventos: [] };
        dias.set(e.fechaEvento, dia);
      }
      dia.eventos.push(e);
    }
    return [...dias.values()];
  });

  tono(estado: string): string {
    if (estado === 'CREADO') return 'aviso';
    if (estado === 'EN CURSO') return 'exito';
    return 'info';
  }

  estadoTexto(estado: string): string {
    return estado === 'CREADO' ? 'Por preparar' : estado.charAt(0) + estado.slice(1).toLowerCase();
  }
}
