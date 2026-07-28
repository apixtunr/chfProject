import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { AuthService } from '../../../core/auth/auth.service';
import { ClienteService } from '../../clientes/cliente.service';
import { ClienteResponse } from '../../clientes/dto/cliente';
import { EventoService } from '../../eventos/evento.service';
import { TipoEventoResponse } from '../../eventos/dto/evento';
import { RentabilidadService } from '../rentabilidad.service';
import { FiltrosRentabilidad, RentabilidadEventoResponse, RentabilidadResumenResponse } from '../dto/rentabilidad';

const PAGINA_URL = '/api/rentabilidad';

@Component({
  selector: 'app-rentabilidad-report',
  imports: [
    ReactiveFormsModule,
    DatePipe,
    DecimalPipe,
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
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
  readonly clientes = signal<ClienteResponse[]>([]);
  readonly tiposEvento = signal<TipoEventoResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);

  readonly puedeExportar: boolean;

  readonly columnas = ['idEvento', 'fechaEvento', 'cliente', 'tipo', 'ingresos', 'costos', 'ganancia', 'porcentaje'];

  readonly formulario = this.fb.group({
    fechaDesde: this.fb.control<string | null>(null),
    fechaHasta: this.fb.control<string | null>(null),
    idCliente: this.fb.control<number | null>(null),
    idTipoEvento: this.fb.control<number | null>(null),
  });

  constructor() {
    this.puedeExportar = this.authService.tienePermiso(PAGINA_URL, 'exportar');
  }

  ngOnInit(): void {
    this.clienteService.listar('', 0, 200).subscribe((p) => this.clientes.set(p.content));
    this.eventoService.listarTiposEvento().subscribe((t) => this.tiposEvento.set(t));
    this.cargar();
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
    this.aplicarFiltros();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.cargar();
  }

  /** Exporta la pagina actual a CSV (abre en Excel). */
  exportarCsv(): void {
    const encabezado = 'Evento,Fecha,Cliente,Tipo,Ingresos,Costos,Ganancia,Porcentaje';
    const lineas = this.filas().map((f) =>
      [
        f.idEvento,
        f.fechaEvento,
        `"${f.clienteNombre.replaceAll('"', '""')}"`,
        `"${f.tipoEventoNombre.replaceAll('"', '""')}"`,
        f.totalIngresos,
        f.totalCostos,
        f.ganancia,
        f.porcentaje,
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
