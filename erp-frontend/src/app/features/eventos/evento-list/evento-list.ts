import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { EventoService } from '../evento.service';
import { EventoResponse } from '../dto/evento';

const PAGINA_URL = '/api/eventos';
const SIN_FILTROS = { fechaDesde: null, fechaHasta: null, idCliente: null, idTipoEvento: null, idEstado: null };

@Component({
  selector: 'app-evento-list',
  imports: [
    CommonModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTooltipModule,
  ],
  templateUrl: './evento-list.html',
  styleUrl: './evento-list.scss',
})
export class EventoList implements OnInit {
  private readonly eventoService = inject(EventoService);
  private readonly authService = inject(AuthService);

  readonly eventos = signal<EventoResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);

  readonly columnas = ['cliente', 'tipo', 'fecha', 'estado', 'acciones'];

  readonly puedeCrear: boolean;

  constructor() {
    this.puedeCrear = this.authService.tienePermiso(PAGINA_URL, 'alta');
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.eventoService.listar(SIN_FILTROS, this.pageIndex(), this.pageSize()).subscribe((page) => {
      this.eventos.set(page.content);
      this.totalElements.set(page.totalElements);
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.cargar();
  }

  colorEstado(estado: string): string {
    switch (estado) {
      case 'FINALIZADO':
        return 'primary';
      case 'CANCELADO':
        return 'warn';
      default:
        return '';
    }
  }
}
