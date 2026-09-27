import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BebidaService } from '../../../core/catalogos/bebida.service';
import { EstadoResponse } from '../../../core/catalogos/estado';
import { EstadoService } from '../../../core/catalogos/estado.service';

const TIPO_ESTADO_GENERAL = 'GENERAL';

@Component({
  selector: 'app-bebida-form',
  imports: [ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  templateUrl: './bebida-form.html',
  styleUrl: './bebida-form.scss',
})
export class BebidaForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly bebidaService = inject(BebidaService);
  private readonly estadoService = inject(EstadoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly idBebida = signal<number | null>(null);
  readonly guardando = signal(false);
  readonly estados = signal<EstadoResponse[]>([]);

  readonly formulario = this.fb.nonNullable.group({
    nombreBebida: ['', [Validators.required, Validators.maxLength(80)]],
    idEstado: this.fb.control<number | null>(null, Validators.required),
  });

  ngOnInit(): void {
    this.estadoService.listarPorTipo(TIPO_ESTADO_GENERAL).subscribe((e) => this.estados.set(e));

    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      return;
    }
    const id = Number(idParam);
    this.idBebida.set(id);
    this.bebidaService.obtener(id).subscribe((bebida) => {
      this.formulario.patchValue({ nombreBebida: bebida.nombreBebida, idEstado: bebida.idEstado });
    });
  }

  guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    const v = this.formulario.getRawValue();
    const request = { nombreBebida: v.nombreBebida.trim(), idEstado: v.idEstado! };

    const id = this.idBebida();
    const operacion = id ? this.bebidaService.actualizar(id, request) : this.bebidaService.crear(request);

    operacion.subscribe({
      next: () => {
        this.snackBar.open(id ? 'Bebida actualizada' : 'Bebida creada', 'Cerrar', { duration: 3000 });
        this.router.navigateByUrl('/menus/bebidas');
      },
      error: () => this.guardando.set(false),
    });
  }
}
