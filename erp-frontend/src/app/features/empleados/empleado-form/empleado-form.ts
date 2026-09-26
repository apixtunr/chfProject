import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { EstadoResponse } from '../../../core/catalogos/estado';
import { EstadoService } from '../../../core/catalogos/estado.service';
import { AdminService } from '../../administracion/admin.service';
import { RolResponse } from '../../administracion/dto/admin';
import { AccesoSistemaRequest, GeneroResponse, PuestoEmpleadoResponse } from '../dto/empleado';
import { EmpleadoService } from '../empleado.service';

const TIPO_ESTADO_GENERAL = 'GENERAL';
const PAGINA_USUARIOS = '/api/usuarios';

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
    MatRadioModule,
    MatIconModule,
  ],
  templateUrl: './empleado-form.html',
  styleUrl: './empleado-form.scss',
})
export class EmpleadoForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly empleadoService = inject(EmpleadoService);
  private readonly estadoService = inject(EstadoService);
  private readonly adminService = inject(AdminService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly idEmpleado = signal<number | null>(null);
  readonly guardando = signal(false);
  readonly puestos = signal<PuestoEmpleadoResponse[]>([]);
  readonly generos = signal<GeneroResponse[]>([]);
  readonly estados = signal<EstadoResponse[]>([]);
  readonly roles = signal<RolResponse[]>([]);

  /** Usuario de esta persona, al editar a alguien que ya lo tiene. */
  readonly usuarioActual = signal<{ idUsuario: number; username: string; rol: string } | null>(null);

  /** Sin permiso para crear usuarios no se ofrece el acceso, para no chocar con un 403. */
  readonly puedeCrearUsuarios = this.authService.tienePermiso(PAGINA_USUARIOS, 'alta');

  readonly formulario = this.crearFormulario();

  /** true cuando se eligio "Si, crearle un usuario" en el alta. */
  get creaAcceso(): boolean {
    return this.formulario.controls.daAcceso.value === true;
  }

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

      // Acceso al sistema. Solo se usa al dar de alta: el acceso de alguien que ya
      // existe se administra desde la pantalla de Usuarios.
      daAcceso: this.fb.control<boolean>(false),
      username: [''],
      password: [''],
      idRolUsuario: this.fb.control<number | null>(null),
    });
  }

  /**
   * Enciende o apaga las reglas de los campos del acceso.
   *
   * Hay que hacerlo a mano porque los tres campos existen siempre en el formulario: si
   * quedaran obligatorios con el acceso apagado, no se podria guardar a un empleado que
   * no entra al sistema, que es el caso mas comun.
   */
  cambiarAcceso(da: boolean): void {
    const c = this.formulario.controls;
    if (da) {
      c.username.setValidators([Validators.required, Validators.maxLength(50)]);
      c.password.setValidators([Validators.required, Validators.minLength(8)]);
      c.idRolUsuario.setValidators([Validators.required]);
    } else {
      c.username.clearValidators();
      c.password.clearValidators();
      c.idRolUsuario.clearValidators();
      c.username.setValue('');
      c.password.setValue('');
      c.idRolUsuario.setValue(null);
    }
    c.username.updateValueAndValidity();
    c.password.updateValueAndValidity();
    c.idRolUsuario.updateValueAndValidity();
  }

  ngOnInit(): void {
    this.empleadoService.listarPuestos().subscribe((p) => this.puestos.set(p));
    this.empleadoService.listarGeneros().subscribe((g) => this.generos.set(g));
    this.estadoService.listarPorTipo(TIPO_ESTADO_GENERAL).subscribe((e) => this.estados.set(e));
    if (this.puedeCrearUsuarios) {
      this.adminService.listarRoles().subscribe((r) => this.roles.set(r));
    }

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
      if (empleado.idUsuario && empleado.username) {
        this.usuarioActual.set({
          idUsuario: empleado.idUsuario,
          username: empleado.username,
          rol: empleado.rolUsuario ?? '',
        });
      }
    });
  }

  guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    const v = this.formulario.getRawValue();
    const acceso: AccesoSistemaRequest | null =
      !this.idEmpleado() && v.daAcceso
        ? { username: v.username.trim(), password: v.password, idRol: v.idRolUsuario! }
        : null;
    const request = {
      nombre: v.nombre,
      apellido: v.apellido,
      idPuestoEmpleado: v.idPuestoEmpleado!,
      idEstado: v.idEstado!,
      idGenero: v.idGenero,
      correo: v.correo || null,
      telefono: v.telefono || null,
      fechaContratacion: v.fechaContratacion ? this.aFechaIso(v.fechaContratacion) : null,
      // Solo viaja en el alta y solo si se pidio. El backend graba empleado y usuario
      // juntos: si el nombre de usuario ya existe, no se crea ninguno de los dos.
      acceso: acceso,
    };

    const id = this.idEmpleado();
    const operacion = id ? this.empleadoService.actualizar(id, request) : this.empleadoService.crear(request);

    operacion.subscribe({
      next: () => {
        const mensaje = id
          ? 'Empleado actualizado'
          : acceso
            ? `Empleado creado con acceso al sistema (${acceso.username})`
            : 'Empleado creado';
        this.snackBar.open(mensaje, 'Cerrar', { duration: 3000 });
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
