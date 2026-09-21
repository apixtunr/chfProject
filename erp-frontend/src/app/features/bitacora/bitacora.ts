import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BitacoraService } from './bitacora.service';
import { BitacoraAccesoResponse, BitacoraMovimientoResponse } from './dto/bitacora';

/** Valores reales que registra AuthService en bitacora_acceso.resultado. */
const RESULTADOS_ACCESO = ['EXITOSO', 'PASSWORD_INCORRECTA', 'USUARIO_INACTIVO', 'USUARIO_BLOQUEADO'];

@Component({
  selector: 'app-bitacora',
  imports: [
    ReactiveFormsModule,
    DatePipe,
    MatTabsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTooltipModule,
  ],
  templateUrl: './bitacora.html',
  styleUrl: './bitacora.scss',
})
export class Bitacora implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly bitacoraService = inject(BitacoraService);

  readonly resultadosAcceso = RESULTADOS_ACCESO;

  // --- Movimientos ---
  readonly movimientos = signal<BitacoraMovimientoResponse[]>([]);
  readonly totalMovimientos = signal(0);
  readonly pageIndexMovimientos = signal(0);
  readonly pageSizeMovimientos = signal(20);
  readonly tablasDisponibles = signal<string[]>([]);
  readonly columnasMovimientos = [
    'fechaMovimiento', 'usuario', 'tablaAfectada', 'registroId', 'operacion',
    'nombreAtributo', 'valorAnterior', 'valorNuevo', 'ipOrigen',
  ];

  readonly filtroMovimientos = this.fb.group({
    tabla: this.fb.control<string | null>(null),
    fechaDesde: this.fb.control<Date | null>(null),
    fechaHasta: this.fb.control<Date | null>(null),
  });

  // --- Accesos ---
  readonly accesos = signal<BitacoraAccesoResponse[]>([]);
  readonly totalAccesos = signal(0);
  readonly pageIndexAccesos = signal(0);
  readonly pageSizeAccesos = signal(20);
  readonly columnasAccesos = ['fechaAcceso', 'usuario', 'accion', 'resultado', 'ipOrigen', 'navegador'];

  readonly filtroAccesos = this.fb.group({
    resultado: this.fb.control<string | null>(null),
    fechaDesde: this.fb.control<Date | null>(null),
    fechaHasta: this.fb.control<Date | null>(null),
  });

  ngOnInit(): void {
    this.bitacoraService.listarTablasConMovimientos().subscribe((t) => this.tablasDisponibles.set(t));
    this.cargarMovimientos();
    this.cargarAccesos();
  }

  cargarMovimientos(): void {
    const v = this.filtroMovimientos.getRawValue();
    this.bitacoraService
      .listarMovimientos(
        { idUsuario: null, tabla: v.tabla, fechaDesde: this.aFecha(v.fechaDesde), fechaHasta: this.aFecha(v.fechaHasta) },
        this.pageIndexMovimientos(),
        this.pageSizeMovimientos(),
      )
      .subscribe((page) => {
        this.movimientos.set(page.content);
        this.totalMovimientos.set(page.totalElements);
      });
  }

  aplicarFiltroMovimientos(): void {
    this.pageIndexMovimientos.set(0);
    this.cargarMovimientos();
  }

  limpiarFiltroMovimientos(): void {
    this.filtroMovimientos.reset();
    this.aplicarFiltroMovimientos();
  }

  onPageChangeMovimientos(event: PageEvent): void {
    this.pageIndexMovimientos.set(event.pageIndex);
    this.pageSizeMovimientos.set(event.pageSize);
    this.cargarMovimientos();
  }

  cargarAccesos(): void {
    const v = this.filtroAccesos.getRawValue();
    this.bitacoraService
      .listarAccesos(
        { idUsuario: null, resultado: v.resultado, fechaDesde: this.aFecha(v.fechaDesde), fechaHasta: this.aFecha(v.fechaHasta) },
        this.pageIndexAccesos(),
        this.pageSizeAccesos(),
      )
      .subscribe((page) => {
        this.accesos.set(page.content);
        this.totalAccesos.set(page.totalElements);
      });
  }

  aplicarFiltroAccesos(): void {
    this.pageIndexAccesos.set(0);
    this.cargarAccesos();
  }

  limpiarFiltroAccesos(): void {
    this.filtroAccesos.reset();
    this.aplicarFiltroAccesos();
  }

  onPageChangeAccesos(event: PageEvent): void {
    this.pageIndexAccesos.set(event.pageIndex);
    this.pageSizeAccesos.set(event.pageSize);
    this.cargarAccesos();
  }

  private aFecha(fecha: Date | null): string | null {
    if (!fecha) {
      return null;
    }
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }
}
