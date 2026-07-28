import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EstadoResponse } from '../../../core/catalogos/estado';
import { EstadoService } from '../../../core/catalogos/estado.service';
import { GeneroResponse, PuestoEmpleadoResponse } from '../dto/empleado';
import { EmpleadoService } from '../empleado.service';

const TIPO_ESTADO_GENERAL = 'GENERAL';

@Component({
  selector: 'app-empleado-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatButtonModule,
  ],
  templateUrl: './empleado-form.html',
  styleUrl: './empleado-form.scss',
})
export class EmpleadoForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly empleadoService = inject(EmpleadoService);
  private readonly estadoService = inject(EstadoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly idEmpleado = signal<number | null>(null);
  readonly guardando = signal(false);
  readonly puestos = signal<PuestoEmpleadoResponse[]>([]);
  readonly generos = signal<GeneroResponse[]>([]);
  readonly estados = signal<EstadoResponse[]>([]);

  readonly formulario = this.crearFormulario();

  private crearFormulario() {
    return this.fb.nonNullable.group({
      nombre: ['', [Validators.required, Validators.maxLength(100)]],
      apellido: ['', [Validators.required, Validators.maxLength(100)]],
      idPuestoEmpleado: this.fb.control<number | null>(null, Validators.required),
      idEstado: this.fb.control<number | null>(null, Validators.required),
      idGenero: this.fb.control<number | null>(null),
      correo: ['', [Validators.email]],
      telefono: ['', [Validators.pattern(/^[0-9+\- ]{8,20}$/)]],
      fechaContratacion: this.fb.control<Date | null>(null),
    });
  }

  ngOnInit(): void {
    this.empleadoService.listarPuestos().subscribe((p) => this.puestos.set(p));
    this.empleadoService.listarGeneros().subscribe((g) => this.generos.set(g));
    this.estadoService.listarPorTipo(TIPO_ESTADO_GENERAL).subscribe((e) => this.estados.set(e));

    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      return;
    }
    const id = Number(idParam);
    this.idEmpleado.set(id);
    this.empleadoService.obtener(id).subscribe((empleado) => {
      this.formulario.patchValue({
        nombre: empleado.nombre,
        apellido: empleado.apellido,
        idPuestoEmpleado: empleado.idPuestoEmpleado,
        idEstado: empleado.idEstado,
        idGenero: empleado.idGenero,
        correo: empleado.correo ?? '',
        telefono: empleado.telefono ?? '',
        fechaContratacion: empleado.fechaContratacion ? new Date(empleado.fechaContratacion) : null,
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
      apellido: v.apellido,
      idPuestoEmpleado: v.idPuestoEmpleado!,
      idEstado: v.idEstado!,
      idGenero: v.idGenero,
      correo: v.correo || null,
      telefono: v.telefono || null,
      fechaContratacion: v.fechaContratacion ? this.aFechaIso(v.fechaContratacion) : null,
    };

    const id = this.idEmpleado();
    const operacion = id ? this.empleadoService.actualizar(id, request) : this.empleadoService.crear(request);

    operacion.subscribe({
      next: () => {
        this.snackBar.open(id ? 'Empleado actualizado' : 'Empleado creado', 'Cerrar', { duration: 3000 });
        this.router.navigateByUrl('/empleados');
      },
      error: () => this.guardando.set(false),
    });
  }

  private aFechaIso(fecha: Date): string {
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }
}
