import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { CotizacionService } from '../cotizaciones/cotizacion.service';
import { EventoResponse } from '../eventos/dto/evento';
import { EventoService } from '../eventos/evento.service';
import { InventarioService } from '../inventario/inventario.service';

@Component({
  selector: 'app-home',
  imports: [RouterLink, MatIconModule, DatePipe],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {
  private readonly authService = inject(AuthService);
  private readonly eventoService = inject(EventoService);
  private readonly cotizacionService = inject(CotizacionService);
  private readonly inventarioService = inject(InventarioService);

  readonly usuario = computed(() => this.authService.usuarioActual());

  readonly saludo = computed(() => {
    const hora = new Date().getHours();
    if (hora < 12) return 'Buenos dias';
    if (hora < 19) return 'Buenas tardes';
    return 'Buenas noches';
  });

  readonly proximoEvento = signal<EventoResponse | null | undefined>(undefined);
  readonly cotizacionesPendientes = signal<number | null>(null);
  readonly alertasInventario = signal<number | null>(null);

  constructor() {
    const puedeVer = (paginaUrl: string) => this.authService.puedeVer(paginaUrl);

    if (puedeVer('/api/eventos')) {
      const hoy = new Date().toISOString().substring(0, 10);
      // Traigo varios (no solo el primero): el mas cercano en fecha puede estar
      // CANCELADO, y ese no cuenta como "proximo evento" real.
      const ESTADOS_VIGENTES = ['PLANIFICADO', 'EN CURSO'];
      const filtrosProximoEvento = { fechaDesde: hoy, fechaHasta: null, idCliente: null, idTipoEvento: null, idEstado: null };
      this.eventoService.listar(filtrosProximoEvento, 0, 10, 'fechaEvento,asc').subscribe({
        next: (page) =>
          this.proximoEvento.set(page.content.find((e) => ESTADOS_VIGENTES.includes(e.estadoNombre)) ?? null),
        error: () => this.proximoEvento.set(null),
      });
    }

    if (puedeVer('/api/cotizaciones')) {
      this.cotizacionService.listar(null, 0, 200).subscribe({
        next: (page) =>
          this.cotizacionesPendientes.set(
            page.content.filter((c) => c.ultimaVersionEstado === 'ENVIADA').length,
          ),
        error: () => this.cotizacionesPendientes.set(null),
      });
    }

    if (puedeVer('/api/inventarios')) {
      this.inventarioService.alertasBajoStock().subscribe({
        next: (alertas) => this.alertasInventario.set(alertas.length),
        error: () => this.alertasInventario.set(null),
      });
    }
  }
}
