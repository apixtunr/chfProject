import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { ColorEstado, COLOR_ESTADO_EVENTO_DEFECTO, COLOR_POR_ESTADO_EVENTO } from '../../eventos/dto/evento';
import { EventoPagoResponse } from '../dto/pago';
import { PagoService } from '../pago.service';


@Component({
  selector: 'app-pago-list',
  imports: [
    RouterLink,
    DatePipe,
    DecimalPipe,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatTooltipModule,
  ],
  templateUrl: './pago-list.html',
  styleUrl: './pago-list.scss',
})
export class PagoList implements OnInit {
  private readonly pagoService = inject(PagoService);

  readonly eventos = signal<EventoPagoResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);
  readonly filtro = signal<string>('PENDIENTE');

  readonly columnas = ['fechaEvento', 'cliente', 'tipo', 'total', 'abonado', 'pendiente', 'estado', 'acciones'];

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.pagoService.listarEventosConSaldo(this.filtro(), this.pageIndex(), this.pageSize()).subscribe((page) => {
      this.eventos.set(page.content);
      this.totalElements.set(page.totalElements);
    });
  }

  filtrar(filtro: string): void {
    this.filtro.set(filtro);
    this.pageIndex.set(0);
    this.cargar();
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
