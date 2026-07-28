import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { EventoService } from '../../eventos/evento.service';
import { EventoResponse } from '../../eventos/dto/evento';
import { PagoService } from '../pago.service';
import { PagoResponse } from '../dto/pago';

const PAGINA_URL = '/api/pagos';

@Component({
  selector: 'app-pago-list',
  imports: [
    RouterLink,
    DatePipe,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
  ],
  templateUrl: './pago-list.html',
  styleUrl: './pago-list.scss',
})
export class PagoList implements OnInit {
  private readonly pagoService = inject(PagoService);
  private readonly eventoService = inject(EventoService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly pagos = signal<PagoResponse[]>([]);
  readonly eventos = signal<EventoResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);
  readonly idEventoFiltro = signal<number | null>(null);

  readonly puedeCrear: boolean;
  readonly puedeEditar: boolean;
  readonly puedeEliminar: boolean;

  constructor() {
    this.puedeCrear = this.authService.tienePermiso(PAGINA_URL, 'alta');
    this.puedeEditar = this.authService.tienePermiso(PAGINA_URL, 'modificacion');
    this.puedeEliminar = this.authService.tienePermiso(PAGINA_URL, 'baja');
  }

  get columnas(): string[] {
    const base = ['fechaPago', 'evento', 'metodo', 'monto', 'estado', 'usuario'];
    return [...base, 'acciones'];
  }

  ngOnInit(): void {
    // Eventos para el filtro (los mas recientes)
    this.eventoService.listar(null, null, 0, 200).subscribe((p) => this.eventos.set(p.content));
    this.cargar();
  }

  cargar(): void {
    this.pagoService.listar(this.idEventoFiltro(), this.pageIndex(), this.pageSize()).subscribe((page) => {
      this.pagos.set(page.content);
      this.totalElements.set(page.totalElements);
    });
  }

  filtrarPorEvento(idEvento: number | null): void {
    this.idEventoFiltro.set(idEvento);
    this.pageIndex.set(0);
    this.cargar();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.cargar();
  }

  etiquetaEvento(e: EventoResponse): string {
    return `#${e.idEvento} · ${e.clienteNombre} · ${e.fechaEvento}`;
  }

  eliminar(pago: PagoResponse): void {
    const ref = this.dialog.open(ConfirmDialog, {
      data: { titulo: 'Eliminar pago', mensaje: `¿Eliminar el pago de Q ${pago.monto} del evento #${pago.idEvento}?` },
    });

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.pagoService.eliminar(pago.idPago).subscribe(() => {
        this.snackBar.open('Pago eliminado', 'Cerrar', { duration: 3000 });
        this.cargar();
      });
    });
  }
}
