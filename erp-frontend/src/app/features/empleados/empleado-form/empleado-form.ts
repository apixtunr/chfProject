import { Component, OnInit, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { EstadoResponse } from '../../../core/catalogos/estado';
import { EstadoService } from '../../../core/catalogos/estado.service';
import { AdminService } from '../../administracion/admin.service';
import { usuarioSegunPolitica } from '../../administracion/usuarios/politica-usuario';
import { RolResponse } from '../../administracion/dto/admin';
import {
  AccesoSistemaRequest,
  GeneroResponse,
  PuestoEmpleadoResponse,
  TIPO_DPI,
  TipoDocumentoResponse,
  formatoDpi,
} from '../dto/empleado';
import { EmpleadoService } from '../empleado.service';
import { SelectBuscable } from '../../../shared/select-buscable';

const TIPO_ESTADO_GENERAL = 'GENERAL';
const PAGINA_USUARIOS = '/api/usuarios';

/** 13 digitos, con los espacios o guiones opcionales del documento fisico. */
const PATRON_DPI = /^\d{4}[ -]?\d{5}[ -]?\d{4}$/;

type FilaDocumento = FormGroup<{
  idTipoDocumento: FormControl<number | null>;
  numeroDocumento: FormControl<string>;
}>;

@Component({
  selector: 'app-empleado-form',
  imports: [SelectBuscable, 
    ReactiveFormsModule,
    RouterLink,
    MatSelectModule,
    MatDatepickerModule,
    MatButtonModule,
    MatRadioModule,
    MatIconModule,
    MatTooltipModule,
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

  /** Tipos de documento que van en la seccion Documentos: todos menos el DPI, que va en los datos. */
  readonly tiposDocumento = signal<TipoDocumentoResponse[]>([]);

  /** Filas de la seccion Documentos (licencia de conducir, etc.). */
  readonly documentos = new FormArray<FilaDocumento>([]);

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
      dpi: ['', [Validators.required, Validators.pattern(PATRON_DPI)]],

      // Acceso al sistema. Solo se usa al dar de alta: el acceso de alguien que ya
      // existe se administra desde la pantalla de Usuarios.
      daAcceso: this.fb.control<boolean>(false),
      password: [''],
      idRolUsuario: this.fb.control<number | null>(null),
    });
  }

  /** Como va a quedar su usuario: nombre.apellido, segun la politica de la empresa. */
  get usuarioPrevisto(): string {
    const { nombre, apellido } = this.formulario.controls;
    return usuarioSegunPolitica(nombre.value, apellido.value);
  }

  /**
   * Enciende o apaga las reglas de los campos del acceso.
   *
   * Hay que hacerlo a mano porque los dos campos existen siempre en el formulario: si
   * quedaran obligatorios con el acceso apagado, no se podria guardar a un empleado que
   * no entra al sistema, que es el caso mas comun.
   */
  cambiarAcceso(da: boolean): void {
    const c = this.formulario.controls;
    if (da) {
      c.password.setValidators([Validators.required, Validators.minLength(8)]);
      c.idRolUsuario.setValidators([Validators.required]);
    } else {
      c.password.clearValidators();
      c.idRolUsuario.clearValidators();
      c.password.setValue('');
      c.idRolUsuario.setValue(null);
    }
    c.password.updateValueAndValidity();
    c.idRolUsuario.updateValueAndValidity();
  }

  agregarDocumento(idTipoDocumento: number | null = null, numeroDocumento = ''): void {
    this.documentos.push(
      this.fb.group({
        idTipoDocumento: this.fb.control<number | null>(idTipoDocumento, Validators.required),
        numeroDocumento: this.fb.nonNullable.control(numeroDocumento, [Validators.required, Validators.maxLength(50)]),
      }),
    );
  }

  quitarDocumento(indice: number): void {
    this.documentos.removeAt(indice);
  }

  /** En cada fila se ofrecen los tipos que no estan elegidos en otra: cada tipo va una sola vez. */
  tiposDisponibles(indice: number): TipoDocumentoResponse[] {
    const usados = this.documentos.controls
      .filter((_, i) => i !== indice)
      .map((fila) => fila.controls.idTipoDocumento.value);
    return this.tiposDocumento().filter((t) => !usados.includes(t.idTipoDocumento));
  }

  /** Ya se eligieron todos los tipos: no hay que ofrecer otra fila. */
  get todosLosTiposUsados(): boolean {
    return this.documentos.length >= this.tiposDocumento().length;
  }

  ngOnInit(): void {
    this.empleadoService
      .listarTiposDocumento()
      .subscribe((tipos) => this.tiposDocumento.set(tipos.filter((t) => t.nombreTipo !== TIPO_DPI)));
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
        dpi: formatoDpi(empleado.dpi),
      });
      for (const d of empleado.documentos ?? []) {
        this.agregarDocumento(d.idTipoDocumento, d.numeroDocumento);
      }
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
    if (this.formulario.invalid || this.documentos.invalid) {
      this.formulario.markAllAsTouched();
      this.documentos.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    const v = this.formulario.getRawValue();
    const acceso: AccesoSistemaRequest | null =
      !this.idEmpleado() && v.daAcceso
        ? { password: v.password, idRol: v.idRolUsuario! }
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
      dpi: v.dpi,
      documentos: this.documentos.getRawValue().map((d) => ({
        idTipoDocumento: d.idTipoDocumento!,
        numeroDocumento: d.numeroDocumento.trim(),
      })),
      // Solo viaja en el alta y solo si se pidio. El backend graba empleado y usuario
      // juntos, y le pone al usuario el nombre segun la politica de la empresa.
      acceso: acceso,
    };

    const id = this.idEmpleado();
    const operacion = id ? this.empleadoService.actualizar(id, request) : this.empleadoService.crear(request);

    operacion.subscribe({
      next: (empleado) => {
        const mensaje = id
          ? 'Empleado actualizado'
          : empleado.username
            ? `Empleado creado. Entra al sistema como ${empleado.username}`
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
