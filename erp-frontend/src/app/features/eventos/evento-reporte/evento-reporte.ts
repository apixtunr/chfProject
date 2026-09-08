import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import type { ChartData } from 'chart.js';
import { EstadoResponse } from '../../../core/catalogos/estado';
import { EstadoService } from '../../../core/catalogos/estado.service';
import { ChartComponent } from '../../../shared/chart/chart';
import { ClienteService } from '../../clientes/cliente.service';
import { ClienteResponse } from '../../clientes/dto/cliente';
import { RentabilidadService } from '../../rentabilidad/rentabilidad.service';
import { RentabilidadResumenResponse } from '../../rentabilidad/dto/rentabilidad';
import { EventoService } from '../evento.service';
import {
  ColorEstado,
  COLOR_ESTADO_EVENTO_DEFECTO,
  COLOR_POR_ESTADO_EVENTO,
  EventoResponse,
  EventoResumenResponse,
  FiltrosEvento,
  TipoEventoResponse,
} from '../dto/evento';

const TIPO_ESTADO_EVENTO = 'EVENTO';
const PALETA_TIPOS = ['#5c6bc0', '#26a69a', '#fb8c00', '#8d6e63', '#7e57c2', '#26c6da', '#ec407a', '#9ccc65', '#5d4037', '#78909c'];

@Component({
  selector: 'app-evento-reporte',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    ChartComponent,
  ],
  templateUrl: './evento-reporte.html',
  styleUrl: './evento-reporte.scss',
})
export class EventoReporte implements OnInit {
  private readonly eventoService = inject(EventoService);
  private readonly rentabilidadService = inject(RentabilidadService);
  private readonly clienteService = inject(ClienteService);
  private readonly estadoService = inject(EstadoService);
  private readonly fb = inject(FormBuilder);

  readonly eventos = signal<EventoResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);

  readonly clientes = signal<ClienteResponse[]>([]);
  readonly tiposEvento = signal<TipoEventoResponse[]>([]);
  readonly estadosEvento = signal<EstadoResponse[]>([]);

  readonly resumen = signal<EventoResumenResponse | null>(null);
  readonly rentabilidadResumen = signal<RentabilidadResumenResponse | null>(null);

  readonly columnas = ['cliente', 'tipo', 'fecha', 'estado'];

  readonly formulario = this.fb.group({
    fechaDesde: this.fb.control<string | null>(null),
    fechaHasta: this.fb.control<string | null>(null),
    idCliente: this.fb.control<number | null>(null),
    idTipoEvento: this.fb.control<number | null>(null),
    idEstado: this.fb.control<number | null>(null),
  });

  readonly datosPorEstado = computed<ChartData<'doughnut'>>(() => {
    const conteos = this.resumen()?.porEstado ?? [];
    return {
      labels: conteos.map((c) => c.etiqueta),
      datasets: [
        {
          data: conteos.map((c) => c.cantidad),
          // Para la grafica se usa el tono de texto (mas saturado), no el fondo pastel del
          // chip: ese fondo se ve casi invisible como relleno solido sobre el card blanco.
          backgroundColor: conteos.map((c) => (COLOR_POR_ESTADO_EVENTO[c.etiqueta] ?? COLOR_ESTADO_EVENTO_DEFECTO).text),
        },
      ],
    };
  });

  readonly datosPorTipo = computed<ChartData<'bar'>>(() => {
    const conteos = this.resumen()?.porTipo ?? [];
    return {
      labels: conteos.map((c) => c.etiqueta),
      datasets: [
        {
          label: 'Eventos',
          data: conteos.map((c) => c.cantidad),
          backgroundColor: conteos.map((_, i) => PALETA_TIPOS[i % PALETA_TIPOS.length]),
        },
      ],
    };
  });

  readonly opcionesBarras = {
    plugins: { legend: { display: false } },
    scales: { y: { beginAtZero: true, ticks: { precision: 0 } } },
  };

  ngOnInit(): void {
    this.clienteService.listar('', 0, 200).subscribe((p) => this.clientes.set(p.content));
    this.eventoService.listarTiposEvento().subscribe((t) => this.tiposEvento.set(t));
    this.estadoService.listarPorTipo(TIPO_ESTADO_EVENTO).subscribe((e) => this.estadosEvento.set(e));
    this.cargar();
  }

  private get filtros(): FiltrosEvento {
    const v = this.formulario.getRawValue();
    return {
      fechaDesde: v.fechaDesde,
      fechaHasta: v.fechaHasta,
      idCliente: v.idCliente,
      idTipoEvento: v.idTipoEvento,
      idEstado: v.idEstado,
    };
  }

  cargar(): void {
    const filtros = this.filtros;
    this.eventoService.listar(filtros, this.pageIndex(), this.pageSize()).subscribe((page) => {
      this.eventos.set(page.content);
      this.totalElements.set(page.totalElements);
    });
    this.eventoService.resumen(filtros).subscribe((r) => this.resumen.set(r));
    this.rentabilidadService
      .resumen({
        fechaDesde: filtros.fechaDesde,
        fechaHasta: filtros.fechaHasta,
        idCliente: filtros.idCliente,
        idTipoEvento: filtros.idTipoEvento,
      })
      .subscribe((r) => this.rentabilidadResumen.set(r));
  }

  aplicarFiltros(): void {
    this.pageIndex.set(0);
    this.cargar();
  }

  limpiarFiltros(): void {
    this.formulario.reset();
    this.aplicarFiltros();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.cargar();
  }

  colorEstado(estado: string): ColorEstado {
    return COLOR_POR_ESTADO_EVENTO[estado.toUpperCase()] ?? COLOR_ESTADO_EVENTO_DEFECTO;
  }
}
