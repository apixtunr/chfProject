import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthService } from '../../../core/auth/auth.service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { CotizacionService } from '../../cotizaciones/cotizacion.service';
import { EventoService } from '../../eventos/evento.service';
import { EventoResponse } from '../../eventos/dto/evento';
import { PagoService } from '../pago.service';
import { PagoResponse } from '../dto/pago';

const PAGINA_URL = '/api/pagos';

interface ResumenEvento {
  total: number;
  abonado: number;
  pendiente: number;
}

@Component({
  selector: 'app-pago-evento',
  imports: [
    RouterLink,
    DatePipe,
    DecimalPipe,
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatTooltipModule,
  ],
  templateUrl: './pago-evento.html',
  styleUrl: './pago-evento.scss',
})
export class PagoEvento implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly eventoService = inject(EventoService);
  private readonly cotizacionService = inject(CotizacionService);
  private readonly pagoService = inject(PagoService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly idEvento: number;
  readonly evento = signal<EventoResponse | null>(null);
  readonly resumen = signal<ResumenEvento | null>(null);
  readonly pagos = signal<PagoResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);

  readonly columnas = ['fechaPago', 'tipo', 'metodo', 'monto', 'estado', 'usuario', 'acciones'];

  readonly puedeCrear: boolean;
  readonly puedeEditar: boolean;
  readonly puedeEliminar: boolean;

  constructor() {
    this.idEvento = Number(this.route.snapshot.paramMap.get('idEvento'));
    this.puedeCrear = this.authService.tienePermiso(PAGINA_URL, 'alta');
    this.puedeEditar = this.authService.tienePermiso(PAGINA_URL, 'modificacion');
    this.puedeEliminar = this.authService.tienePermiso(PAGINA_URL, 'baja');
  }

  ngOnInit(): void {
    this.cargarEvento();
    this.cargarPagos();
  }

  private cargarEvento(): void {
    this.eventoService.obtener(this.idEvento).subscribe((evento) => {
      this.evento.set(evento);
      const total$ = evento.idCotizacionVersion
        ? this.cotizacionService.obtenerVersion(evento.idCotizacionVersion).pipe(map((v) => v.montoTotal))
        : of(evento.montoMenu);
      total$.subscribe((total) => this.recalcularResumen(total));
    });
  }

  private recalcularResumen(total: number): void {
    this.pagoService.listar(this.idEvento, 0, 200).subscribe((pagina) => {
      const abonado = pagina.content
        .filter((p) => p.idCostoEvento === null && p.estadoNombre !== 'ANULADO')
        .reduce((acc, p) => acc + p.monto, 0);
      this.resumen.set({ total, abonado, pendiente: total - abonado });
    });
  }

  private cargarPagos(): void {
    this.pagoService.listar(this.idEvento, this.pageIndex(), this.pageSize()).subscribe((page) => {
      this.pagos.set(page.content);
      this.totalElements.set(page.totalElements);
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.cargarPagos();
  }

  etiquetaTipo(p: PagoResponse): string {
    return p.idCostoEvento !== null ? 'Reembolso de costo' : 'Abono';
  }

  eliminar(pago: PagoResponse): void {
    const ref = this.dialog.open(ConfirmDialog, {
      data: { titulo: 'Eliminar pago', mensaje: `¿Eliminar el pago de Q ${pago.monto}?` },
    });

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.pagoService.eliminar(pago.idPago).subscribe(() => {
        this.snackBar.open('Pago eliminado', 'Cerrar', { duration: 3000 });
        this.cargarEvento();
        this.cargarPagos();
      });
    });
  }
}
