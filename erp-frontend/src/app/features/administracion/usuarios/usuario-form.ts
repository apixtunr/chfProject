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
import { EmpleadoService } from '../../empleados/empleado.service';
import { EmpleadoResponse } from '../../empleados/dto/empleado';
import { AdminService } from '../admin.service';
import { RolResponse } from '../dto/admin';
import { usuarioSegunPolitica } from './politica-usuario';

const TIPO_ESTADO_GENERAL = 'GENERAL';

@Component({
  selector: 'app-usuario-form',
  imports: [ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  templateUrl: './usuario-form.html',
  styleUrl: './usuario-form.scss',
})
export class UsuarioForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly adminService = inject(AdminService);
  private readonly estadoService = inject(EstadoService);
  private readonly empleadoService = inject(EmpleadoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly idUsuario = signal<number | null>(null);
  readonly guardando = signal(false);
  readonly roles = signal<RolResponse[]>([]);
  readonly estados = signal<EstadoResponse[]>([]);
  readonly empleados = signal<EmpleadoResponse[]>([]);

  readonly formulario = this.fb.nonNullable.group({
    // No se escribe: sale del nombre del empleado (politica de la empresa) y solo se muestra.
    username: this.fb.nonNullable.control({ value: '', disabled: true }),
    password: ['', [Validators.required, Validators.minLength(8)]],
    idRol: this.fb.control<number | null>(null, Validators.required),
    idEstado: this.fb.control<number | null>(null, Validators.required),
    // Obligatorio: todo usuario es una persona del negocio. De ahi sale su nombre
    // completo, que la tabla usuario no guarda aparte para no duplicarlo.
    idEmpleado: this.fb.control<number | null>(null, Validators.required),
  });

  ngOnInit(): void {
    this.adminService.listarRoles().subscribe((r) => this.roles.set(r));
    this.estadoService.listarPorTipo(TIPO_ESTADO_GENERAL).subscribe((e) => this.estados.set(e));
    this.empleadoService.listar('', 0, 200).subscribe((p) => {
      this.empleados.set(p.content);
      this.mostrarUsuarioPrevisto();
    });
    this.formulario.controls.idEmpleado.valueChanges.subscribe(() => this.mostrarUsuarioPrevisto());

    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      // Al llegar desde la ficha de un empleado ("Crear acceso al sistema") viene ya
      // elegido, para no obligar a buscarlo otra vez en la lista.
      const empleadoParam = this.route.snapshot.queryParamMap.get('empleado');
      if (empleadoParam) {
        this.formulario.controls.idEmpleado.setValue(Number(empleadoParam));
      }
      return;
    }
    const id = Number(idParam);
    this.idUsuario.set(id);
    // En edicion no se cambia la contrasena aqui (endpoint dedicado) y el usuario nunca.
    this.formulario.controls.password.disable();
    this.adminService.obtenerUsuario(id).subscribe((u) => {
      this.formulario.patchValue({
        username: u.username,
        idRol: u.idRol,
        idEstado: u.idEstado,
        idEmpleado: u.idEmpleado,
      });
    });
  }

  /**
   * En un alta muestra como se va a llamar el usuario segun el empleado elegido. En una
   * edicion se deja el que ya tiene: el nombre de usuario no cambia aunque cambie el empleado.
   */
  private mostrarUsuarioPrevisto(): void {
    if (this.idUsuario()) {
      return;
    }
    const empleado = this.empleados().find((e) => e.idEmpleado === this.formulario.controls.idEmpleado.value);
    this.formulario.controls.username.setValue(empleado ? usuarioSegunPolitica(empleado.nombre, empleado.apellido) : '');
  }

  guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    const v = this.formulario.getRawValue();
    const id = this.idUsuario();

    const operacion = id
      ? this.adminService.actualizarUsuario(id, {
          idRol: v.idRol!,
          idEstado: v.idEstado!,
          idEmpleado: v.idEmpleado,
        })
      : this.adminService.crearUsuario({
          password: v.password,
          idRol: v.idRol!,
          idEstado: v.idEstado!,
          idEmpleado: v.idEmpleado,
        });

    operacion.subscribe({
      next: (usuario) => {
        this.snackBar.open(id ? 'Usuario actualizado' : `Usuario creado: ${usuario.username}`, 'Cerrar', {
          duration: 3000,
        });
        this.router.navigateByUrl('/admin/usuarios');
      },
      error: () => this.guardando.set(false),
    });
  }
}
