import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin, Observable } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { AdminService } from '../admin.service';
import { RolOpcionResponse } from '../dto/admin';

const PAGINA_URL = '/api/roles';

/** Fila editable de la matriz: una opcion del sistema con sus banderas. */
interface FilaPermiso {
  idOpcion: number;
  moduloNombre: string;
  nombreOpcion: string;
  paginaUrl: string | null;
  /** true si el rol tiene acceso a esta opcion (existe fila rol_opcion). */
  acceso: boolean;
  alta: boolean;
  baja: boolean;
  modificacion: boolean;
  imprimir: boolean;
  exportar: boolean;
  /** Estado original, para calcular el diff al guardar. */
  original: { acceso: boolean; alta: boolean; baja: boolean; modificacion: boolean; imprimir: boolean; exportar: boolean };
}

@Component({
  selector: 'app-rol-permisos',
  imports: [RouterLink, FormsModule, MatTableModule, MatCheckboxModule, MatButtonModule, MatIconModule],
  templateUrl: './rol-permisos.html',
  styleUrl: './rol-permisos.scss',
})
export class RolPermisos implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);

  readonly filas = signal<FilaPermiso[]>([]);
  readonly nombreRol = signal('');
  readonly guardando = signal(false);

  /** Filas agrupadas por modulo (jerarquia modulo -> opciones), en el orden que devuelve el backend. */
  readonly grupos = computed(() => {
    const porModulo = new Map<string, FilaPermiso[]>();
    for (const fila of this.filas()) {
      const lista = porModulo.get(fila.moduloNombre) ?? [];
      lista.push(fila);
      porModulo.set(fila.moduloNombre, lista);
    }
    return [...porModulo.entries()].map(([modulo, filas]) => ({ modulo, filas }));
  });

  readonly puedeGuardar: boolean;

  readonly columnas = ['opcion', 'acceso', 'alta', 'modificacion', 'baja', 'imprimir', 'exportar'];

  private idRol!: number;

  constructor() {
    this.puedeGuardar =
      this.authService.tienePermiso(PAGINA_URL, 'alta') || this.authService.tienePermiso(PAGINA_URL, 'modificacion');
  }

  ngOnInit(): void {
    this.idRol = Number(this.route.snapshot.paramMap.get('id'));
    this.cargar();
  }

  cargar(): void {
    forkJoin({
      roles: this.adminService.listarRoles(),
      opciones: this.adminService.listarOpciones(),
      permisos: this.adminService.listarPermisosDeRol(this.idRol),
    }).subscribe(({ roles, opciones, permisos }) => {
      this.nombreRol.set(roles.find((r) => r.idRol === this.idRol)?.nombreRol ?? `#${this.idRol}`);

      const permisosPorOpcion = new Map<number, RolOpcionResponse>(permisos.map((p) => [p.idOpcion, p]));
      this.filas.set(
        opciones.map((o) => {
          const p = permisosPorOpcion.get(o.idOpcion);
          const estado = {
            acceso: !!p,
            alta: p?.alta ?? false,
            baja: p?.baja ?? false,
            modificacion: p?.modificacion ?? false,
            imprimir: p?.imprimir ?? false,
            exportar: p?.exportar ?? false,
          };
          return {
            idOpcion: o.idOpcion,
            moduloNombre: o.moduloNombre,
            nombreOpcion: o.nombreOpcion,
            paginaUrl: o.paginaUrl,
            ...estado,
            original: { ...estado },
          };
        }),
      );
    });
  }

  /** Al quitar el acceso se apagan todas las banderas; al darlo no se enciende ninguna (solo lectura). */
  onAccesoChange(fila: FilaPermiso): void {
    if (!fila.acceso) {
      fila.alta = fila.baja = fila.modificacion = fila.imprimir = fila.exportar = false;
    }
  }

  /** Encender cualquier bandera implica tener acceso. */
  onBanderaChange(fila: FilaPermiso): void {
    if (fila.alta || fila.baja || fila.modificacion || fila.imprimir || fila.exportar) {
      fila.acceso = true;
    }
  }

  guardar(): void {
    const operaciones: Observable<unknown>[] = [];

    for (const f of this.filas()) {
      const cambio =
        f.acceso !== f.original.acceso ||
        f.alta !== f.original.alta ||
        f.baja !== f.original.baja ||
        f.modificacion !== f.original.modificacion ||
        f.imprimir !== f.original.imprimir ||
        f.exportar !== f.original.exportar;
      if (!cambio) {
        continue;
      }

      const request = {
        alta: f.alta,
        baja: f.baja,
        modificacion: f.modificacion,
        imprimir: f.imprimir,
        exportar: f.exportar,
      };

      if (f.acceso && !f.original.acceso) {
        operaciones.push(this.adminService.asignarPermiso(this.idRol, f.idOpcion, request));
      } else if (f.acceso) {
        operaciones.push(this.adminService.actualizarPermiso(this.idRol, f.idOpcion, request));
      } else {
        operaciones.push(this.adminService.quitarPermiso(this.idRol, f.idOpcion));
      }
    }

    if (operaciones.length === 0) {
      this.snackBar.open('No hay cambios que guardar', 'Cerrar', { duration: 3000 });
      return;
    }

    this.guardando.set(true);
    forkJoin(operaciones).subscribe({
      next: () => {
        this.snackBar.open(`Permisos guardados (${operaciones.length} cambio(s))`, 'Cerrar', { duration: 3000 });
        this.guardando.set(false);
        this.cargar();
      },
      error: () => this.guardando.set(false),
    });
  }
}
