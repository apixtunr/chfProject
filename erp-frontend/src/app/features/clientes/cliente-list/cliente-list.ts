import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { ClienteService } from '../cliente.service';
import { ClienteResponse, EstadoClienteFiltro } from '../dto/cliente';

const PAGINA_URL = '/api/clientes';

@Component({
  selector: 'app-cliente-list',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatTooltipModule,
  ],
  templateUrl: './cliente-list.html',
  styleUrl: './cliente-list.scss',
})
export class ClienteList implements OnInit {
  private readonly clienteService = inject(ClienteService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly clientes = signal<ClienteResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);

  readonly busqueda = new FormControl('', { nonNullable: true });
  /** Por defecto solo activos: los inactivos se consultan a proposito. */
  readonly estado = signal<EstadoClienteFiltro>('ACTIVO');
  readonly filtrosEstado: { valor: EstadoClienteFiltro; etiqueta: string }[] = [
    { valor: 'ACTIVO', etiqueta: 'Activos' },
    { valor: 'INACTIVO', etiqueta: 'Inactivos' },
    { valor: 'TODOS', etiqueta: 'Todos' },
  ];

  readonly puedeCrear: boolean;
  readonly puedeEditar: boolean;
  /** Permiso de BAJA: antes eliminaba, ahora inactiva y reactiva. */
  readonly puedeCambiarEstado: boolean;

  constructor() {
    this.puedeCrear = this.authService.tienePermiso(PAGINA_URL, 'alta');
    this.puedeEditar = this.authService.tienePermiso(PAGINA_URL, 'modificacion');
    this.puedeCambiarEstado = this.authService.tienePermiso(PAGINA_URL, 'baja');
  }

  get columnas(): string[] {
    const base = ['nombre', 'nit', 'telefono', 'correo', 'direccion'];
    if (this.estado() !== 'ACTIVO') base.push('estado');
    return this.puedeEditar || this.puedeCambiarEstado ? [...base, 'acciones'] : base;
  }

  ngOnInit(): void {
    this.cargar();

    this.busqueda.valueChanges.pipe(debounceTime(300), distinctUntilChanged()).subscribe(() => this.buscar());
  }

  cargar(): void {
    this.clienteService.listar(this.busqueda.value.trim(), this.pageIndex(), this.pageSize(), this.estado()).subscribe((page) => {
      this.clientes.set(page.content);
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

  filtrarEstado(estado: EstadoClienteFiltro): void {
    this.estado.set(estado);
    this.buscar();
  }

  /**
   * Inactivar pide confirmacion porque saca al cliente de cotizaciones y eventos nuevos;
   * el backend lo rechaza si todavia tiene negocio abierto y el mensaje llega por el
   * interceptor de errores. Reactivar no pide confirmacion: no quita nada.
   */
  cambiarEstado(cliente: ClienteResponse): void {
    if (cliente.activo) {
      const ref = this.dialog.open(ConfirmDialog, {
        data: {
          titulo: 'Inactivar cliente',
          mensaje: `"${cliente.nombre}" dejará de aparecer al crear cotizaciones y eventos. Su historial se conserva y puede reactivarlo cuando quiera.`,
        },
      });
      ref.afterClosed().subscribe((confirmado) => {
        if (confirmado) this.aplicarEstado(cliente, false);
      });
    } else {
      this.aplicarEstado(cliente, true);
    }
  }

  private aplicarEstado(cliente: ClienteResponse, activo: boolean): void {
    this.clienteService.cambiarEstado(cliente.idCliente, activo).subscribe(() => {
      this.snackBar.open(activo ? 'Cliente reactivado' : 'Cliente inactivado', 'Cerrar', { duration: 3000 });
      this.cargar();
    });
  }
}
