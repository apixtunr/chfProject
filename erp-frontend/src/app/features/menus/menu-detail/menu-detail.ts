import { DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { MenuPlatoService } from '../menu-plato.service';
import { MenuPlatoResponse, MenuResponse, PlatoResponse } from '../dto/menu';

const PAGINA_URL = '/api/menus';

@Component({
  selector: 'app-menu-detail',
  imports: [
    RouterLink,
    DecimalPipe,
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './menu-detail.html',
  styleUrl: './menu-detail.scss',
})
export class MenuDetail implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly menuPlatoService = inject(MenuPlatoService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly menu = signal<MenuResponse | null>(null);
  readonly platosDelMenu = signal<MenuPlatoResponse[]>([]);
  readonly todosLosPlatos = signal<PlatoResponse[]>([]);
  readonly agregando = signal(false);

  /** Platos que aun no estan en el menu (para el selector). */
  readonly platosDisponibles = computed(() => {
    const enMenu = new Set(this.platosDelMenu().map((mp) => mp.idPlato));
    return this.todosLosPlatos().filter((p) => !enMenu.has(p.idPlato));
  });

  readonly precioTotal = computed(() =>
    this.platosDelMenu().reduce((suma, mp) => suma + Number(mp.precioUnitario), 0),
  );

  readonly puedeAgregar: boolean;
  readonly puedeEditar: boolean;
  readonly puedeEliminar: boolean;

  readonly columnas = ['ordenMenu', 'nombrePlato', 'precioUnitario', 'acciones'];

  readonly formularioPlato = this.fb.nonNullable.group({
    idPlato: this.fb.control<number | null>(null, Validators.required),
    precioUnitario: this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]),
    ordenMenu: this.fb.control<number | null>(null),
  });

  private idMenu!: number;

  constructor() {
    this.puedeAgregar = this.authService.tienePermiso(PAGINA_URL, 'alta');
    this.puedeEditar = this.authService.tienePermiso(PAGINA_URL, 'modificacion');
    this.puedeEliminar = this.authService.tienePermiso(PAGINA_URL, 'baja');
  }

  ngOnInit(): void {
    this.idMenu = Number(this.route.snapshot.paramMap.get('id'));
    this.menuPlatoService.obtenerMenu(this.idMenu).subscribe((m) => this.menu.set(m));
    this.menuPlatoService.listarPlatos('', 0, 200).subscribe((p) => this.todosLosPlatos.set(p.content));
    this.cargarPlatos();
  }

  cargarPlatos(): void {
    this.menuPlatoService.listarPlatosDeMenu(this.idMenu).subscribe((platos) => this.platosDelMenu.set(platos));
  }

  agregarPlato(): void {
    if (this.formularioPlato.invalid) {
      this.formularioPlato.markAllAsTouched();
      return;
    }
    const v = this.formularioPlato.getRawValue();
    this.menuPlatoService
      .agregarPlatoAMenu(this.idMenu, v.idPlato!, {
        precioUnitario: v.precioUnitario!,
        ordenMenu: v.ordenMenu,
      })
      .subscribe(() => {
        this.snackBar.open('Plato agregado al menu', 'Cerrar', { duration: 3000 });
        this.formularioPlato.reset();
        this.agregando.set(false);
        this.cargarPlatos();
      });
  }

  actualizarPrecio(mp: MenuPlatoResponse, valor: string): void {
    const precio = Number(valor);
    if (isNaN(precio) || precio < 0 || precio === Number(mp.precioUnitario)) {
      return;
    }
    this.menuPlatoService
      .actualizarPlatoDeMenu(this.idMenu, mp.idPlato, { precioUnitario: precio, ordenMenu: mp.ordenMenu })
      .subscribe(() => {
        this.snackBar.open('Precio actualizado', 'Cerrar', { duration: 2000 });
        this.cargarPlatos();
      });
  }

  quitarPlato(mp: MenuPlatoResponse): void {
    const ref = this.dialog.open(ConfirmDialog, {
      data: { titulo: 'Quitar plato', mensaje: `¿Quitar "${mp.nombrePlato}" del menu?` },
    });

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.menuPlatoService.quitarPlatoDeMenu(this.idMenu, mp.idPlato).subscribe(() => {
        this.snackBar.open('Plato quitado del menu', 'Cerrar', { duration: 3000 });
        this.cargarPlatos();
      });
    });
  }
}
