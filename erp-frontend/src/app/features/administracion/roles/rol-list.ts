import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { AdminService } from '../admin.service';
import { RolResponse } from '../dto/admin';

const PAGINA_URL = '/api/roles';

@Component({
  selector: 'app-rol-list',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    MatTableModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,

  MatTooltipModule,  ],
  templateUrl: './rol-list.html',
  styleUrl: './rol-list.scss',
})
export class RolList implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly adminService = inject(AdminService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly roles = signal<RolResponse[]>([]);
  readonly creando = signal(false);
  readonly editando = signal<RolResponse | null>(null);

  readonly puedeCrear: boolean;
  readonly puedeEditar: boolean;
  readonly puedeEliminar: boolean;

  readonly columnas = ['nombreRol', 'acciones'];

  readonly formulario = this.fb.nonNullable.group({
    nombreRol: ['', [Validators.required, Validators.maxLength(50)]],
  });

  constructor() {
    this.puedeCrear = this.authService.tienePermiso(PAGINA_URL, 'alta');
    this.puedeEditar = this.authService.tienePermiso(PAGINA_URL, 'modificacion');
    this.puedeEliminar = this.authService.tienePermiso(PAGINA_URL, 'baja');
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.adminService.listarRoles().subscribe((r) => this.roles.set(r));
  }

  iniciarCreacion(): void {
    this.editando.set(null);
    this.formulario.reset();
    this.creando.set(true);
  }

  iniciarEdicion(rol: RolResponse): void {
    this.editando.set(rol);
    this.formulario.patchValue({ nombreRol: rol.nombreRol });
    this.creando.set(true);
  }

  guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const request = { nombreRol: this.formulario.getRawValue().nombreRol.trim().toUpperCase() };
    const rolEnEdicion = this.editando();
    const operacion = rolEnEdicion
      ? this.adminService.actualizarRol(rolEnEdicion.idRol, request)
      : this.adminService.crearRol(request);

    operacion.subscribe(() => {
      this.snackBar.open(rolEnEdicion ? 'Rol actualizado' : 'Rol creado', 'Cerrar', { duration: 3000 });
      this.creando.set(false);
      this.cargar();
    });
  }

  eliminar(rol: RolResponse): void {
    const ref = this.dialog.open(ConfirmDialog, {
      data: { titulo: 'Eliminar rol', mensaje: `¿Eliminar el rol "${rol.nombreRol}"? Sus permisos se pierden.` },
    });

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.adminService.eliminarRol(rol.idRol).subscribe(() => {
        this.snackBar.open('Rol eliminado', 'Cerrar', { duration: 3000 });
        this.cargar();
      });
    });
  }
}
