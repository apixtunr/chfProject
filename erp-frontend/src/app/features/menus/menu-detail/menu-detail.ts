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
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { MenuPlatoService } from '../menu-plato.service';
import { PRECIO_POR, UnidadVenta } from '../../../core/catalogos/menu';
import { MenuPlatoResponse, MenuResponse, PlatoResponse } from '../dto/menu';
import { TablaResponsiva } from '../../../shared/tabla-responsiva';

const PAGINA_URL = '/api/menus';

@Component({
  selector: 'app-menu-detail',
  imports: [TablaResponsiva, 
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

  MatTooltipModule,  ],
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

  readonly puedeAgregar: boolean;
  readonly puedeEditar: boolean;
  readonly puedeEliminar: boolean;

  readonly columnas = ['ordenMenu', 'nombrePlato', 'precioUnitario', 'precioDesde100', 'acciones'];
  /** "por persona", "el ciento" o "c/u", como se escribe junto al precio. */
  precioPor(unidad: UnidadVenta): string {
    return PRECIO_POR[unidad];
  }

  readonly formularioPlato = this.fb.nonNullable.group({
    idPlato: this.fb.control<number | null>(null, Validators.required),
    precioUnitario: this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]),
    // Opcional: sin el, el plato cuesta lo mismo sin importar el tamano del evento.
    precioDesde100: this.fb.control<number | null>(null, [Validators.min(0)]),
    ordenMenu: this.fb.control<number | null>(null),
  });

  /** Unidad de venta del plato elegido en el formulario, para rotular el precio ("el ciento"). */
  unidadSeleccionada(): UnidadVenta {
    const id = this.formularioPlato.controls.idPlato.value;
    return this.todosLosPlatos().find((p) => p.idPlato === id)?.unidadVenta ?? 'PERSONA';
  }

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
        precioDesde100: v.precioDesde100,
        ordenMenu: v.ordenMenu,
      })
      .subscribe(() => {
        this.snackBar.open('Plato agregado al menu', 'Cerrar', { duration: 3000 });
        this.formularioPlato.reset();
        this.agregando.set(false);
        this.cargarPlatos();
      });
  }

  /**
   * Cambia uno de los dos precios desde la tabla y reenvia el otro tal como esta. Dejar
   * vacio el de 100 personas lo quita: el plato queda con un solo precio.
   */
  actualizarPrecio(mp: MenuPlatoResponse, campo: 'precioUnitario' | 'precioDesde100', valor: string): void {
    const precio = valor.trim() === '' ? null : Number(valor);
    const actual = mp[campo] == null ? null : Number(mp[campo]);
    if ((precio != null && (isNaN(precio) || precio < 0)) || precio === actual) {
      return;
    }
    if (campo === 'precioUnitario' && precio == null) {
      this.cargarPlatos();
      return;
    }
    const cambios = {
      precioUnitario: campo === 'precioUnitario' ? precio! : Number(mp.precioUnitario),
      precioDesde100: campo === 'precioDesde100' ? precio : mp.precioDesde100,
      ordenMenu: mp.ordenMenu,
    };
    this.menuPlatoService.actualizarPlatoDeMenu(this.idMenu, mp.idPlato, cambios).subscribe({
      next: () => {
        this.snackBar.open('Precio actualizado', 'Cerrar', { duration: 2000 });
        this.cargarPlatos();
      },
      // Si el backend lo rechaza (ej. el de 100 mas caro que el base), vuelve a mostrar lo guardado.
      error: () => this.cargarPlatos(),
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
