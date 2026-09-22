import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DepartamentoResponse } from '../../../core/catalogos/departamento';
import { DepartamentoService } from '../../../core/catalogos/departamento.service';
import { MunicipioResponse } from '../../../core/catalogos/municipio';
import { MunicipioService } from '../../../core/catalogos/municipio.service';
import { ClienteService } from '../cliente.service';

@Component({
  selector: 'app-cliente-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './cliente-form.html',
  styleUrl: './cliente-form.scss',
})
export class ClienteForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly clienteService = inject(ClienteService);
  private readonly departamentoService = inject(DepartamentoService);
  private readonly municipioService = inject(MunicipioService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly idCliente = signal<number | null>(null);
  readonly guardando = signal(false);
  readonly departamentos = signal<DepartamentoResponse[]>([]);
  readonly municipios = signal<MunicipioResponse[]>([]);

  readonly formulario = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    correo: ['', [Validators.email, Validators.maxLength(150)]],
    telefono: ['', [Validators.pattern(/^[0-9+\- ]{8,20}$/)]],
    nit: ['', [Validators.maxLength(20)]],
    direccion: ['', [Validators.required, Validators.maxLength(255)]],
    // El departamento solo filtra la lista de municipios; lo que se guarda es el municipio.
    idDepartamento: this.fb.control<number | null>(null, Validators.required),
    idMunicipio: this.fb.control<number | null>(null, Validators.required),
  });

  ngOnInit(): void {
    this.departamentoService.listar().subscribe((d) => this.departamentos.set(d));

    this.formulario.controls.idDepartamento.valueChanges.subscribe((idDepartamento) => {
      this.formulario.controls.idMunicipio.setValue(null);
      this.municipios.set([]);
      if (idDepartamento) {
        this.municipioService.listar(idDepartamento).subscribe((m) => this.municipios.set(m));
      }
    });

    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      return;
    }
    const id = Number(idParam);
    this.idCliente.set(id);
    this.clienteService.obtener(id).subscribe((cliente) => {
      this.formulario.patchValue({
        nombre: cliente.nombre,
        correo: cliente.correo ?? '',
        telefono: cliente.telefono ?? '',
        nit: cliente.nit ?? '',
        direccion: cliente.direccion,
        idDepartamento: cliente.idDepartamento,
      });
      // El municipio se asigna hasta tener cargada su lista, que depende del departamento.
      this.municipioService.listar(cliente.idDepartamento).subscribe((m) => {
        this.municipios.set(m);
        this.formulario.controls.idMunicipio.setValue(cliente.idMunicipio);
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
      nombre: v.nombre,
      correo: v.correo,
      telefono: v.telefono,
      nit: v.nit,
      direccion: v.direccion,
      idMunicipio: v.idMunicipio!,
    };
    const id = this.idCliente();
    const operacion = id ? this.clienteService.actualizar(id, request) : this.clienteService.crear(request);

    operacion.subscribe({
      next: () => {
        this.snackBar.open(id ? 'Cliente actualizado' : 'Cliente creado', 'Cerrar', { duration: 3000 });
        this.router.navigateByUrl('/clientes');
      },
      error: () => this.guardando.set(false),
    });
  }
}
