import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EstadoResponse } from '../../../core/catalogos/estado';
import { EstadoService } from '../../../core/catalogos/estado.service';
import { BebidaResumen } from '../../../core/catalogos/bebida';
import { BebidaService } from '../../../core/catalogos/bebida.service';
import { UnidadVenta } from '../../../core/catalogos/menu';
import { MenuPlatoService } from '../menu-plato.service';
import { SelectBuscable } from '../../../shared/select-buscable';

const TIPO_ESTADO_GENERAL = 'GENERAL';

@Component({
  selector: 'app-plato-form',
  imports: [SelectBuscable, ReactiveFormsModule, RouterLink, MatSelectModule, MatButtonModule, MatIconModule],
  templateUrl: './plato-form.html',
  styleUrl: './plato-form.scss',
})
export class PlatoForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly menuPlatoService = inject(MenuPlatoService);
  private readonly estadoService = inject(EstadoService);
  private readonly bebidaService = inject(BebidaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly idPlato = signal<number | null>(null);
  readonly guardando = signal(false);
  readonly estados = signal<EstadoResponse[]>([]);

  /** Las activas del catalogo, mas las que ya tenga el plato aunque se hayan inactivado. */
  readonly bebidasDisponibles = signal<BebidaResumen[]>([]);

  readonly formulario = this.fb.nonNullable.group({
    nombrePlato: ['', [Validators.required, Validators.maxLength(120)]],
    idEstado: this.fb.control<number | null>(null, Validators.required),
    unidadVenta: this.fb.nonNullable.control<UnidadVenta>('PERSONA', Validators.required),
    // Bebidas que incluye el plato; ninguna = no lleva bebida (boquitas).
    idsBebida: this.fb.nonNullable.control<number[]>([]),
  });

  ngOnInit(): void {
    this.estadoService.listarPorTipo(TIPO_ESTADO_GENERAL).subscribe((e) => this.estados.set(e));
    this.bebidaService.listarActivas().subscribe((activas) => this.agregarOpciones(activas));

    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      return;
    }
    const id = Number(idParam);
    this.idPlato.set(id);
    this.menuPlatoService.obtenerPlato(id).subscribe((plato) => {
      this.formulario.patchValue({
        nombrePlato: plato.nombrePlato,
        idEstado: plato.idEstado,
        unidadVenta: plato.unidadVenta,
        idsBebida: plato.bebidas.map((b) => b.idBebida),
      });
      this.agregarOpciones(plato.bebidas);
    });
  }

  private agregarOpciones(bebidas: BebidaResumen[]): void {
    this.bebidasDisponibles.update((actuales) => {
      const nuevas = bebidas
        .filter((b) => !actuales.some((a) => a.idBebida === b.idBebida))
        .map((b) => ({ idBebida: b.idBebida, nombreBebida: b.nombreBebida }));
      return [...actuales, ...nuevas].sort((a, b) => a.nombreBebida.localeCompare(b.nombreBebida));
    });
  }

  guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    const v = this.formulario.getRawValue();
    const request = {
      nombrePlato: v.nombrePlato.trim(),
      idEstado: v.idEstado!,
      unidadVenta: v.unidadVenta,
      idsBebida: v.idsBebida,
    };

    const id = this.idPlato();
    const operacion = id ? this.menuPlatoService.actualizarPlato(id, request) : this.menuPlatoService.crearPlato(request);

    operacion.subscribe({
      next: () => {
        this.snackBar.open(id ? 'Plato actualizado' : 'Plato creado', 'Cerrar', { duration: 3000 });
        this.router.navigateByUrl('/menus/platos');
      },
      error: () => this.guardando.set(false),
    });
  }
}
