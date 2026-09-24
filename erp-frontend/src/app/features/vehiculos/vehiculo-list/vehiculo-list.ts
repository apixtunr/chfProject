import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { VehiculoService } from '../vehiculo.service';
import { VehiculoResponse } from '../dto/vehiculo';

const PAGINA_URL = '/api/vehiculos';

@Component({
  selector: 'app-vehiculo-list',
  imports: [
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatTooltipModule,
  ],
  templateUrl: './vehiculo-list.html',
  styleUrl: './vehiculo-list.scss',
})
export class VehiculoList implements OnInit {
  private readonly vehiculoService = inject(VehiculoService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly vehiculos = signal<VehiculoResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);

  readonly puedeCrear: boolean;
  readonly puedeEditar: boolean;
  readonly puedeEliminar: boolean;

  constructor() {
    this.puedeCrear = this.authService.tienePermiso(PAGINA_URL, 'alta');
    this.puedeEditar = this.authService.tienePermiso(PAGINA_URL, 'modificacion');
    this.puedeEliminar = this.authService.tienePermiso(PAGINA_URL, 'baja');
  }

  get columnas(): string[] {
    const base = ['placa', 'marca', 'linea', 'tipoPlaca', 'estado'];
    return this.puedeEditar || this.puedeEliminar ? [...base, 'acciones'] : base;
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.vehiculoService.listar(this.pageIndex(), this.pageSize()).subscribe((page) => {
      this.vehiculos.set(page.content);
      this.totalElements.set(page.totalElements);
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.cargar();
  }

  eliminar(vehiculo: VehiculoResponse): void {
    const ref = this.dialog.open(ConfirmDialog, {
      data: { titulo: 'Eliminar vehiculo', mensaje: `¿Eliminar el vehiculo "${vehiculo.placa}"?` },
    });

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.vehiculoService.eliminar(vehiculo.idVehiculo).subscribe(() => {
        this.snackBar.open('Vehiculo eliminado', 'Cerrar', { duration: 3000 });
        this.cargar();
      });
    });
  }
}
