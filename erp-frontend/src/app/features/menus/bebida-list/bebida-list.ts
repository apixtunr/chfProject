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
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, debounceTime } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { BebidaResponse } from '../../../core/catalogos/bebida';
import { BebidaService } from '../../../core/catalogos/bebida.service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { TablaResponsiva } from '../../../shared/tabla-responsiva';

const PAGINA_URL = '/api/bebidas';

@Component({
  selector: 'app-bebida-list',
  imports: [TablaResponsiva, 
    RouterLink,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule,
  ],
  templateUrl: './bebida-list.html',
  styleUrl: './bebida-list.scss',
})
export class BebidaList implements OnInit {
  private readonly bebidaService = inject(BebidaService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly bebidas = signal<BebidaResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);
  filtroNombre = '';

  readonly puedeCrear: boolean;
  readonly puedeEditar: boolean;
  readonly puedeEliminar: boolean;

  constructor() {
    this.puedeCrear = this.authService.tienePermiso(PAGINA_URL, 'alta');
    this.puedeEditar = this.authService.tienePermiso(PAGINA_URL, 'modificacion');
    this.puedeEliminar = this.authService.tienePermiso(PAGINA_URL, 'baja');
  }

  get columnas(): string[] {
    const base = ['nombreBebida', 'estado'];
    return this.puedeEditar || this.puedeEliminar ? [...base, 'acciones'] : base;
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.bebidaService.listar(this.filtroNombre.trim(), this.pageIndex(), this.pageSize()).subscribe((page) => {
      this.bebidas.set(page.content);
      this.totalElements.set(page.totalElements);
    });
  }

  /** Mientras se escribe, la tabla se actualiza sola al dejar de teclear (como en Clientes). */
  private readonly escritura = new Subject<void>();
  private readonly busquedaAlEscribir = this.escritura
    .pipe(debounceTime(300), takeUntilDestroyed())
    .subscribe(() => this.buscar());

  buscarAlEscribir(): void {
    this.escritura.next();
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

  eliminar(bebida: BebidaResponse): void {
    const ref = this.dialog.open(ConfirmDialog, {
      data: { titulo: 'Eliminar bebida', mensaje: `¿Eliminar la bebida "${bebida.nombreBebida}"?` },
    });

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.bebidaService.eliminar(bebida.idBebida).subscribe(() => {
        this.snackBar.open('Bebida eliminada', 'Cerrar', { duration: 3000 });
        this.cargar();
      });
    });
  }
}
