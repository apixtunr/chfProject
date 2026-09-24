import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { EmpleadoService } from '../empleado.service';
import { EmpleadoResponse } from '../dto/empleado';

const PAGINA_URL = '/api/empleados';

@Component({
  selector: 'app-empleado-list',
  imports: [
    FormsModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,

  MatTooltipModule,  ],
  templateUrl: './empleado-list.html',
  styleUrl: './empleado-list.scss',
})
export class EmpleadoList implements OnInit {
  private readonly empleadoService = inject(EmpleadoService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly empleados = signal<EmpleadoResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);
  readonly filtroNombre = signal('');

  readonly puedeCrear: boolean;
  readonly puedeEditar: boolean;
  readonly puedeEliminar: boolean;

  constructor() {
    this.puedeCrear = this.authService.tienePermiso(PAGINA_URL, 'alta');
    this.puedeEditar = this.authService.tienePermiso(PAGINA_URL, 'modificacion');
    this.puedeEliminar = this.authService.tienePermiso(PAGINA_URL, 'baja');
  }

  get columnas(): string[] {
    const base = ['nombre', 'puesto', 'estado', 'telefono'];
    return this.puedeEditar || this.puedeEliminar ? [...base, 'acciones'] : base;
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.empleadoService.listar(this.filtroNombre(), this.pageIndex(), this.pageSize()).subscribe((page) => {
      this.empleados.set(page.content);
      this.totalElements.set(page.totalElements);
    });
  }

  buscar(): void {
    this.pageIndex.set(0);
    this.cargar();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.cargar();
  }

  eliminar(empleado: EmpleadoResponse): void {
    const ref = this.dialog.open(ConfirmDialog, {
      data: {
        titulo: 'Eliminar empleado',
        mensaje: `¿Eliminar a "${empleado.nombre} ${empleado.apellido}"? Esta accion no se puede deshacer.`,
      },
    });

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.empleadoService.eliminar(empleado.idEmpleado).subscribe(() => {
        this.snackBar.open('Empleado eliminado', 'Cerrar', { duration: 3000 });
        this.cargar();
      });
    });
  }
}
