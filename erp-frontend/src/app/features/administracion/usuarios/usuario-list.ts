import { DatePipe } from '@angular/common';
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
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { AdminService } from '../admin.service';
import { UsuarioResponse } from '../dto/admin';
import { PasswordDialog } from './password-dialog';

const PAGINA_URL = '/api/usuarios';

@Component({
  selector: 'app-usuario-list',
  imports: [
    RouterLink,
    FormsModule,
    DatePipe,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './usuario-list.html',
  styleUrl: './usuario-list.scss',
})
export class UsuarioList implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly usuarios = signal<UsuarioResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);
  filtroUsername = '';

  readonly puedeCrear: boolean;
  readonly puedeEditar: boolean;
  readonly puedeEliminar: boolean;

  readonly columnas = ['username', 'rol', 'estado', 'empleado', 'ultimoAcceso', 'acciones'];

  constructor() {
    this.puedeCrear = this.authService.tienePermiso(PAGINA_URL, 'alta');
    this.puedeEditar = this.authService.tienePermiso(PAGINA_URL, 'modificacion');
    this.puedeEliminar = this.authService.tienePermiso(PAGINA_URL, 'baja');
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.adminService
      .listarUsuarios(this.filtroUsername.trim(), this.pageIndex(), this.pageSize())
      .subscribe((page) => {
        this.usuarios.set(page.content);
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

  estaBloqueado(u: UsuarioResponse): boolean {
    return u.intentosAcceso >= 5;
  }

  desbloquear(u: UsuarioResponse): void {
    this.adminService.desbloquearUsuario(u.idUsuario).subscribe(() => {
      this.snackBar.open(`Usuario ${u.username} desbloqueado`, 'Cerrar', { duration: 3000 });
      this.cargar();
    });
  }

  cambiarPassword(u: UsuarioResponse): void {
    const ref = this.dialog.open(PasswordDialog, { data: u, width: '380px' });
    ref.afterClosed().subscribe((password: string | null) => {
      if (!password) {
        return;
      }
      this.adminService.cambiarPassword(u.idUsuario, password).subscribe(() => {
        this.snackBar.open('Contrasena actualizada', 'Cerrar', { duration: 3000 });
      });
    });
  }

  eliminar(u: UsuarioResponse): void {
    const ref = this.dialog.open(ConfirmDialog, {
      data: { titulo: 'Eliminar usuario', mensaje: `¿Eliminar el usuario "${u.username}"?` },
    });

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.adminService.eliminarUsuario(u.idUsuario).subscribe(() => {
        this.snackBar.open('Usuario eliminado', 'Cerrar', { duration: 3000 });
        this.cargar();
      });
    });
  }
}
