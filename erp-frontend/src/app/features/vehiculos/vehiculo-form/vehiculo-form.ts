import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EstadoResponse } from '../../../core/catalogos/estado';
import { EstadoService } from '../../../core/catalogos/estado.service';
import { LineaVehiculoResponse, TipoPlacaResponse } from '../dto/vehiculo';
import { VehiculoService } from '../vehiculo.service';

const TIPO_ESTADO_VEHICULO = 'VEHICULO';

@Component({
  selector: 'app-vehiculo-form',
  imports: [ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  templateUrl: './vehiculo-form.html',
  styleUrl: './vehiculo-form.scss',
})
export class VehiculoForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly vehiculoService = inject(VehiculoService);
  private readonly estadoService = inject(EstadoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly idVehiculo = signal<number | null>(null);
  readonly guardando = signal(false);
  readonly lineas = signal<LineaVehiculoResponse[]>([]);
  readonly tiposPlaca = signal<TipoPlacaResponse[]>([]);
  readonly estados = signal<EstadoResponse[]>([]);

  readonly formulario = this.crearFormulario();

  private crearFormulario() {
    return this.fb.nonNullable.group({
      placa: ['', [Validators.required, Validators.maxLength(20)]],
      idLineaVehiculo: this.fb.control<number | null>(null, Validators.required),
      idTipoPlaca: this.fb.control<number | null>(null, Validators.required),
      idEstado: this.fb.control<number | null>(null, Validators.required),
      anioVehiculo: this.fb.control<number | null>(null),
    });
  }

  ngOnInit(): void {
    this.vehiculoService.listarLineas().subscribe((l) => this.lineas.set(l));
    this.vehiculoService.listarTiposPlaca().subscribe((t) => this.tiposPlaca.set(t));
    this.estadoService.listarPorTipo(TIPO_ESTADO_VEHICULO).subscribe((e) => this.estados.set(e));

    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      return;
    }
    const id = Number(idParam);
    this.idVehiculo.set(id);
    this.vehiculoService.obtener(id).subscribe((vehiculo) => {
      this.formulario.patchValue({
        placa: vehiculo.placa,
        idLineaVehiculo: vehiculo.idLineaVehiculo,
        idTipoPlaca: vehiculo.idTipoPlaca,
        idEstado: vehiculo.idEstado,
        anioVehiculo: vehiculo.anioVehiculo,
      });
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
      placa: v.placa,
      idLineaVehiculo: v.idLineaVehiculo!,
      idTipoPlaca: v.idTipoPlaca!,
      idEstado: v.idEstado!,
      anioVehiculo: v.anioVehiculo,
    };

    const id = this.idVehiculo();
    const operacion = id ? this.vehiculoService.actualizar(id, request) : this.vehiculoService.crear(request);

    operacion.subscribe({
      next: () => {
        this.snackBar.open(id ? 'Vehiculo actualizado' : 'Vehiculo creado', 'Cerrar', { duration: 3000 });
        this.router.navigateByUrl('/vehiculos');
      },
      error: () => this.guardando.set(false),
    });
  }
}
