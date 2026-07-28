import { HttpClient } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { API_URL } from '../../../core/api-config';
import { AuthService } from '../../../core/auth/auth.service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { CATALOGOS, CampoCatalogo, CatalogoConfig } from './catalogos-config';

/** Fila generica: los catalogos tienen formas distintas, se manejan como objetos planos. */
type Fila = Record<string, unknown>;

/**
 * CRUD generico para todos los catalogos simples de administracion.
 * La configuracion (endpoint, campos, permisos) viene de catalogos-config.ts
 * segun el parametro :catalogo de la ruta.
 */
@Component({
  selector: 'app-catalogo-generico',
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
    MatSelectModule,
    MatCheckboxModule,
  ],
  templateUrl: './catalogo-generico.html',
  styleUrl: './catalogo-generico.scss',
})
export class CatalogoGenerico implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  config!: CatalogoConfig;
  formulario!: FormGroup;

  readonly filas = signal<Fila[]>([]);
  readonly opcionesPorCampo = signal<Record<string, Fila[]>>({});
  readonly editandoId = signal<number | null>(null);
  readonly mostrandoForm = signal(false);

  puedeCrear = false;
  puedeEditar = false;
  puedeEliminar = false;
  columnas: string[] = [];

  ngOnInit(): void {
    // La ruta reutiliza el componente entre catalogos: reaccionar al cambio de parametro
    this.route.paramMap.subscribe((params) => {
      const id = params.get('catalogo');
      const config = CATALOGOS.find((c) => c.id === id);
      if (!config) {
        this.router.navigateByUrl('/admin/catalogos');
        return;
      }
      this.iniciar(config);
    });
  }

  private iniciar(config: CatalogoConfig): void {
    this.config = config;
    this.puedeCrear = this.authService.tienePermiso(config.paginaUrl, 'alta');
    this.puedeEditar = this.authService.tienePermiso(config.paginaUrl, 'modificacion');
    this.puedeEliminar = this.authService.tienePermiso(config.paginaUrl, 'baja');
    this.columnas = [
      ...config.campos.map((c) => c.displayKey ?? c.key),
      ...(this.puedeEditar || this.puedeEliminar ? ['acciones'] : []),
    ];

    // Formulario dinamico segun los campos configurados
    const controles: Record<string, FormControl> = {};
    for (const campo of config.campos) {
      const validadores = [];
      if (campo.requerido) {
        validadores.push(Validators.required);
      }
      if (campo.maxLength) {
        validadores.push(Validators.maxLength(campo.maxLength));
      }
      controles[campo.key] = new FormControl(campo.tipo === 'checkbox' ? false : null, validadores);
    }
    this.formulario = new FormGroup(controles);
    this.mostrandoForm.set(false);
    this.editandoId.set(null);

    // Opciones de los selects
    const opciones: Record<string, Fila[]> = {};
    this.opcionesPorCampo.set(opciones);
    for (const campo of config.campos) {
      if (campo.tipo === 'select' && campo.opcionesUrl) {
        this.http.get<Fila[]>(`${API_URL}${campo.opcionesUrl}`).subscribe((lista) => {
          this.opcionesPorCampo.update((actual) => ({ ...actual, [campo.key]: lista }));
        });
      }
    }

    this.cargar();
  }

  cargar(): void {
    this.http.get<Fila[]>(`${API_URL}${this.config.endpoint}`).subscribe((lista) => this.filas.set(lista));
  }

  valorCelda(fila: Fila, campo: CampoCatalogo): string {
    const valor = fila[campo.displayKey ?? campo.key];
    if (campo.tipo === 'checkbox') {
      return valor ? 'Si' : 'No';
    }
    return valor === null || valor === undefined ? '-' : String(valor);
  }

  campoDeColumna(columna: string): CampoCatalogo {
    return this.config.campos.find((c) => (c.displayKey ?? c.key) === columna)!;
  }

  iniciarCreacion(): void {
    this.editandoId.set(null);
    this.formulario.reset();
    for (const campo of this.config.campos) {
      if (campo.tipo === 'checkbox') {
        this.formulario.get(campo.key)?.setValue(false);
      }
    }
    this.mostrandoForm.set(true);
  }

  iniciarEdicion(fila: Fila): void {
    this.editandoId.set(Number(fila[this.config.idKey]));
    const valores: Fila = {};
    for (const campo of this.config.campos) {
      valores[campo.key] = fila[campo.key] ?? (campo.tipo === 'checkbox' ? false : null);
    }
    this.formulario.patchValue(valores);
    this.mostrandoForm.set(true);
  }

  guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const payload: Fila = {};
    for (const campo of this.config.campos) {
      let valor = this.formulario.get(campo.key)?.value;
      if (campo.tipo === 'texto' && typeof valor === 'string') {
        valor = valor.trim() || null;
      }
      payload[campo.key] = valor;
    }

    const id = this.editandoId();
    const peticion = id
      ? this.http.put(`${API_URL}${this.config.endpoint}/${id}`, payload)
      : this.http.post(`${API_URL}${this.config.endpoint}`, payload);

    peticion.subscribe(() => {
      this.snackBar.open(id ? 'Registro actualizado' : 'Registro creado', 'Cerrar', { duration: 3000 });
      this.mostrandoForm.set(false);
      this.cargar();
    });
  }

  eliminar(fila: Fila): void {
    const id = Number(fila[this.config.idKey]);
    const ref = this.dialog.open(ConfirmDialog, {
      data: { titulo: `Eliminar registro`, mensaje: `¿Eliminar este registro de ${this.config.titulo.toLowerCase()}?` },
    });

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.http.delete(`${API_URL}${this.config.endpoint}/${id}`).subscribe(() => {
        this.snackBar.open('Registro eliminado', 'Cerrar', { duration: 3000 });
        this.cargar();
      });
    });
  }
}
