import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { debounceTime, distinctUntilChanged, of, switchMap } from 'rxjs';
import type { ChartData } from 'chart.js';
import { AuthService } from '../../../core/auth/auth.service';
import { ChartComponent } from '../../../shared/chart/chart';
import { ClienteService } from '../../clientes/cliente.service';
import { ClienteResponse } from '../../clientes/dto/cliente';
import { EventoService } from '../../eventos/evento.service';
import { TipoEventoResponse } from '../../eventos/dto/evento';
import { RentabilidadService } from '../rentabilidad.service';
import { FiltrosRentabilidad, RentabilidadEventoResponse, RentabilidadResumenResponse } from '../dto/rentabilidad';
import { TablaResponsiva } from '../../../shared/tabla-responsiva';
import { SelectBuscable } from '../../../shared/select-buscable';

const PAGINA_URL = '/api/rentabilidad';

/** Colores de los rubros de costo, los mismos de los indicadores. */
const COLOR_PERSONAL = '#3b82f6';
const COLOR_INVENTARIO = '#f59e0b';
const COLOR_EXTRA = '#8b5cf6';

/** Semaforo del margen de un evento: verde desde 30%, amarillo desde 15%, rojo por debajo. */
const MARGEN_BUENO = 30;
const MARGEN_REGULAR = 15;

@Component({
  selector: 'app-rentabilidad-report',
  imports: [SelectBuscable, TablaResponsiva,
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatAutocompleteModule,
    ChartComponent,
  ],
  templateUrl: './rentabilidad-report.html',
  styleUrl: './rentabilidad-report.scss',
})
export class RentabilidadReport implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly rentabilidadService = inject(RentabilidadService);
  private readonly clienteService = inject(ClienteService);
  private readonly eventoService = inject(EventoService);
  private readonly authService = inject(AuthService);

  readonly filas = signal<RentabilidadEventoResponse[]>([]);
  readonly resumen = signal<RentabilidadResumenResponse | null>(null);
  readonly tiposEvento = signal<TipoEventoResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);

  /** Autocomplete de cliente, igual que en el Reporte de eventos. */
  readonly busquedaCliente = new FormControl('', { nonNullable: true });
  readonly clientesFiltrados = signal<ClienteResponse[]>([]);

  readonly puedeExportar: boolean;

  readonly columnas = ['cliente', 'tipo', 'fecha', 'acordado', 'cobrado', 'costos', 'ganancia', 'margen'];

  readonly formulario = this.fb.group({
    fechaDesde: this.fb.control<string | null>(null),
    fechaHasta: this.fb.control<string | null>(null),
    idCliente: this.fb.control<number | null>(null),
    idTipoEvento: this.fb.control<number | null>(null),
  });

  /** Los tres rubros de costo, para la dona y su leyenda. */
  readonly rubrosCosto = computed(() => {
    const r = this.resumen();
    if (!r) {
      return [];
    }
    return [
      { nombre: 'Personal', monto: r.costoPersonal, color: COLOR_PERSONAL },
      { nombre: 'Inventario', monto: r.costoInventario, color: COLOR_INVENTARIO },
      { nombre: 'Costos extra', monto: r.costoExtra, color: COLOR_EXTRA },
    ];
  });

  readonly datosCostos = computed<ChartData<'doughnut'>>(() => ({
    labels: this.rubrosCosto().map((c) => c.nombre),
    datasets: [{ data: this.rubrosCosto().map((c) => c.monto), backgroundColor: this.rubrosCosto().map((c) => c.color) }],
  }));

  readonly datosPorTipo = computed<ChartData<'bar'>>(() => {
    const tipos = this.resumen()?.porTipo ?? [];
    return {
      labels: tipos.map((t) => t.tipoEventoNombre),
      datasets: [
        {
          label: 'Ganancia (Q)',
          data: tipos.map((t) => t.gananciaAcordada),
          backgroundColor: tipos.map((t) => (t.gananciaAcordada < 0 ? '#f43f5e' : '#10b981')),
        },
      ],
    };
  });

  /** El tipo de evento que mas ganancia dejo (vienen ordenados de mayor a menor). */
  readonly masRentable = computed(() => this.resumen()?.porTipo?.[0] ?? null);

  readonly opcionesDona = {
    cutout: '68%',
    plugins: { legend: { display: false } },
  };

  readonly opcionesBarras = {
    plugins: { legend: { display: false } },
    scales: { y: { beginAtZero: true } },
  };

  constructor() {
    this.puedeExportar = this.authService.tienePermiso(PAGINA_URL, 'exportar');
  }

  ngOnInit(): void {
    this.eventoService.listarTiposEvento().subscribe((t) => this.tiposEvento.set(t));

    this.busquedaCliente.valueChanges.subscribe((texto) => {
      if (typeof texto === 'string' && !texto.trim()) {
        this.formulario.controls.idCliente.setValue(null);
        this.clientesFiltrados.set([]);
      }
    });
    this.busquedaCliente.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((texto) => {
          const valor = typeof texto === 'string' ? texto.trim() : '';
          return valor ? this.clienteService.listar(valor, 0, 10) : of(null);
        }),
      )
      .subscribe((page) => this.clientesFiltrados.set(page?.content ?? []));

    this.cargar();
  }

  mostrarCliente(cliente: ClienteResponse | string | null): string {
    if (!cliente || typeof cliente === 'string') return '';
    return cliente.nombre;
  }

  seleccionarCliente(event: MatAutocompleteSelectedEvent): void {
    const valor = event.option.value as ClienteResponse | null;
    this.formulario.controls.idCliente.setValue(valor ? valor.idCliente : null);
  }

  private get filtros(): FiltrosRentabilidad {
    const v = this.formulario.getRawValue();
    return {
      fechaDesde: v.fechaDesde,
      fechaHasta: v.fechaHasta,
      idCliente: v.idCliente,
      idTipoEvento: v.idTipoEvento,
    };
  }

  cargar(): void {
    this.rentabilidadService.listarPorEvento(this.filtros, this.pageIndex(), this.pageSize()).subscribe((page) => {
      this.filas.set(page.content);
      this.totalElements.set(page.totalElements);
    });
    this.rentabilidadService.resumen(this.filtros).subscribe((r) => this.resumen.set(r));
  }

  aplicarFiltros(): void {
    this.pageIndex.set(0);
    this.cargar();
  }

  limpiarFiltros(): void {
    this.formulario.reset();
    this.busquedaCliente.setValue('');
    this.clientesFiltrados.set([]);
    this.aplicarFiltros();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.cargar();
  }

  /** Porcentaje de un rubro sobre el total de costos, para la leyenda de la dona. */
  porcentajeCosto(monto: number): number {
    const total = this.resumen()?.totalCostos ?? 0;
    return total > 0 ? (monto / total) * 100 : 0;
  }

  semaforo(margen: number): 'bueno' | 'regular' | 'bajo' {
    return margen >= MARGEN_BUENO ? 'bueno' : margen >= MARGEN_REGULAR ? 'regular' : 'bajo';
  }

  anchoBarraMargen(margen: number): number {
    return Math.min(Math.max(margen, 0), 100);
  }

  /** Exporta la pagina actual a CSV (abre en Excel). */
  exportarCsv(): void {
    const encabezado =
      'Evento,Fecha,Cliente,Tipo,Acordado,Cobrado,Por cobrar,Personal,Inventario,Costos extra,Costos,Ganancia acordada,Ganancia cobrada,Margen';
    const texto = (valor: string) => `"${valor.replaceAll('"', '""')}"`;
    const lineas = this.filas().map((f) =>
      [
        f.idEvento,
        f.fechaEvento,
        texto(f.clienteNombre),
        texto(f.tipoEventoNombre),
        f.ingresosAcordados,
        f.cobrado,
        f.porCobrar,
        f.costoPersonal,
        f.costoInventario,
        f.costoExtra,
        f.totalCostos,
        f.gananciaAcordada,
        f.gananciaCobrada,
        f.margen,
      ].join(','),
    );
    // BOM para que Excel reconozca UTF-8 (tildes y enies)
    const blob = new Blob(['﻿' + [encabezado, ...lineas].join('\n')], { type: 'text/csv;charset=utf-8' });
    const enlace = document.createElement('a');
    enlace.href = URL.createObjectURL(blob);
    enlace.download = `rentabilidad_${new Date().toISOString().slice(0, 10)}.csv`;
    enlace.click();
    URL.revokeObjectURL(enlace.href);
  }
}
