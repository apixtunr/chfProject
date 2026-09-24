import { Component, computed, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { TipoPermiso } from '../../core/models/auth';
import { PanelInicio } from './dto/inicio';
import { InicioService } from './inicio.service';
import { fechaLarga, fechaLocalHoy, monto } from './inicio-formato';
import { PanelAgenda } from './panel-agenda/panel-agenda';
import { PanelPendientes } from './panel-pendientes/panel-pendientes';
import { PanelPreparacion } from './panel-preparacion/panel-preparacion';

interface AccionRapida {
  etiqueta: string;
  icono: string;
  ruta: string;
  paginaUrl: string;
  permiso: TipoPermiso | 'ver';
}

interface Indicador {
  etiqueta: string;
  valor: string;
  detalle: string;
  icono: string;
  ruta: string;
  /** Se pinta en ambar solo cuando hay algo que alguien tiene que resolver. */
  atencion: boolean;
}

/**
 * Acciones rapidas en orden de prioridad. Se muestran las tres primeras que el rol puede
 * usar: asi Ventas ve "Nueva cotizacion", Bodega "Movimiento de inventario", Cocina
 * "Nuevo plato", sin tener que configurar nada por rol.
 */
const ACCIONES: AccionRapida[] = [
  { etiqueta: 'Nueva cotización', icono: 'request_quote', ruta: '/cotizaciones/nueva', paginaUrl: '/api/cotizaciones', permiso: 'alta' },
  { etiqueta: 'Nuevo evento', icono: 'event', ruta: '/eventos/nuevo', paginaUrl: '/api/eventos', permiso: 'alta' },
  { etiqueta: 'Movimiento de inventario', icono: 'swap_vert', ruta: '/inventario/movimientos/nuevo', paginaUrl: '/api/movimientos-inventario', permiso: 'alta' },
  { etiqueta: 'Registrar pago', icono: 'payments', ruta: '/pagos', paginaUrl: '/api/pagos', permiso: 'alta' },
  { etiqueta: 'Nuevo plato', icono: 'restaurant', ruta: '/menus/platos/nuevo', paginaUrl: '/api/platos', permiso: 'alta' },
  { etiqueta: 'Nuevo menú', icono: 'restaurant_menu', ruta: '/menus/nuevo', paginaUrl: '/api/menus', permiso: 'alta' },
  { etiqueta: 'Rentabilidad', icono: 'trending_up', ruta: '/rentabilidad', paginaUrl: '/api/rentabilidad', permiso: 'ver' },
];

/**
 * Pantalla de inicio: un panel de trabajo por rol. Todo sale de una sola llamada a
 * GET /api/inicio, que ya viene recortada a lo que el rol puede ver (una seccion en null
 * no se dibuja). Los paneles grandes son componentes aparte para que cada hoja de estilos
 * quede dentro del presupuesto de angular.json.
 */
@Component({
  selector: 'app-home',
  imports: [RouterLink, MatIconModule, PanelPreparacion, PanelAgenda, PanelPendientes],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {
  private readonly authService = inject(AuthService);
  private readonly inicioService = inject(InicioService);

  readonly panel = signal<PanelInicio | null>(null);
  readonly cargando = signal(true);
  readonly error = signal(false);

  /**
   * La fecha de referencia la da el backend. Antes se calculaba con toISOString(), que
   * devuelve la fecha en UTC: despues de las 18:00 en Guatemala ya era "manana" y los
   * eventos del dia desaparecian del inicio.
   */
  readonly hoy = computed(() => this.panel()?.fechaReferencia ?? fechaLocalHoy());
  readonly fecha = computed(() => fechaLarga(this.hoy()));

  readonly saludo = computed(() => {
    const hora = new Date().getHours();
    const saludo = hora < 12 ? 'Buenos días' : hora < 19 ? 'Buenas tardes' : 'Buenas noches';
    const nombre = this.authService.usuarioActual()?.nombreCompleto?.split(' ')[0];
    return nombre ? `${saludo}, ${nombre}` : saludo;
  });

  readonly acciones = computed(() =>
    ACCIONES.filter((a) =>
      a.permiso === 'ver' ? this.authService.puedeVer(a.paginaUrl) : this.authService.tienePermiso(a.paginaUrl, a.permiso),
    ).slice(0, 3),
  );

  /** Una frase con el estado del dia; se arma con lo que el rol puede ver. */
  readonly resumenDelDia = computed(() => {
    const r = this.panel()?.resumen;
    if (!r) return '';
    const partes: string[] = [];
    if (r.eventosSemana !== null) {
      partes.push(r.eventosSemana ? `${this.plural(r.eventosSemana, 'evento', 'eventos')} en los próximos 7 días` : 'Sin eventos en los próximos 7 días');
    }
    if (r.eventosPorPreparar) {
      partes.push(`${r.eventosPorPreparar} ${r.eventosPorPreparar === 1 ? 'necesita' : 'necesitan'} preparación`);
    }
    if (r.productosBajoStock) {
      partes.push(`${this.plural(r.productosBajoStock, 'producto', 'productos')} en su mínimo`);
    }
    if (r.cotizacionesEnviadas) {
      partes.push(`${this.plural(r.cotizacionesEnviadas, 'cotización', 'cotizaciones')} sin respuesta`);
    }
    return partes.length ? partes.join(' · ') : 'Todo está al día.';
  });

  readonly indicadores = computed<Indicador[]>(() => {
    const r = this.panel()?.resumen;
    if (!r) return [];
    const lista: Indicador[] = [];
    if (r.eventosSemana !== null) {
      lista.push({ etiqueta: 'Eventos esta semana', valor: `${r.eventosSemana}`, detalle: 'Próximos 7 días', icono: 'calendar_month', ruta: '/eventos', atencion: false });
    }
    if (r.eventosPorPreparar !== null) {
      lista.push({
        etiqueta: 'Por preparar',
        valor: `${r.eventosPorPreparar}`,
        detalle: r.eventosPorPreparar ? 'Aún sin planificar' : 'Todos planificados',
        icono: 'pending_actions',
        ruta: '/eventos',
        atencion: r.eventosPorPreparar > 0,
      });
    }
    if (r.cotizacionesEnviadas !== null) {
      lista.push({ etiqueta: 'Cotizaciones por responder', valor: `${r.cotizacionesEnviadas}`, detalle: 'Enviadas al cliente', icono: 'request_quote', ruta: '/cotizaciones', atencion: false });
    }
    if (r.productosBajoStock !== null) {
      lista.push({
        etiqueta: 'Productos en su mínimo',
        valor: `${r.productosBajoStock}`,
        detalle: r.productosBajoStock ? 'Requieren reposición' : 'Stock en orden',
        icono: 'inventory_2',
        ruta: '/inventario',
        atencion: r.productosBajoStock > 0,
      });
    }
    if (r.saldoPendiente !== null) {
      lista.push({
        etiqueta: 'Saldo por cobrar',
        valor: `Q ${monto(r.saldoPendiente)}`,
        detalle: r.eventosConSaldo ? `En ${this.plural(r.eventosConSaldo, 'evento', 'eventos')}` : 'Sin saldos pendientes',
        icono: 'account_balance_wallet',
        ruta: '/pagos',
        atencion: false,
      });
    }
    return lista;
  });

  /** Hay al menos una tarjeta de pendientes que aplica al rol. */
  readonly conPendientes = computed(() => {
    const p = this.panel();
    return (
      !!p &&
      [p.insumosFaltantes, p.bajoStock, p.cotizacionesEnviadas, p.cotizacionesAceptadasSinEvento, p.cobrosPendientes, p.actividad].some(
        (s) => s !== null,
      )
    );
  });

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(false);
    this.inicioService.panel().subscribe({
      next: (panel) => {
        this.panel.set(panel);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set(true);
        this.cargando.set(false);
      },
    });
  }

  private plural(n: number, singular: string, plural: string): string {
    return `${n} ${n === 1 ? singular : plural}`;
  }
}
