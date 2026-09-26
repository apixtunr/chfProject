import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DepartamentoResponse } from '../../../core/catalogos/departamento';
import { DepartamentoService } from '../../../core/catalogos/departamento.service';
import { MunicipioResponse } from '../../../core/catalogos/municipio';
import { MunicipioService } from '../../../core/catalogos/municipio.service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { ClienteService } from '../cliente.service';
import { PosibleDuplicado } from '../dto/cliente';
import { alMenosUnoValidator, nitValidator, normalizarNit } from '../nit-guatemala';

/**
 * Datos que se revisan contra otros clientes. Solo el NIT bloquea: telefono y correo se
 * pueden compartir (una familia, una organizadora, una empresa con sucursales), asi que
 * solo avisan.
 */
type CampoRevisado = 'nit' | 'telefono' | 'correo';
/** Como los nombra el backend en PosibleDuplicado.coincidencias. */
const ETIQUETA_CAMPO: Record<CampoRevisado, string> = { nit: 'NIT', telefono: 'teléfono', correo: 'correo' };

@Component({
  selector: 'app-cliente-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatIconModule,
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
  private readonly dialog = inject(MatDialog);

  readonly idCliente = signal<number | null>(null);
  readonly guardando = signal(false);
  readonly departamentos = signal<DepartamentoResponse[]>([]);
  readonly municipios = signal<MunicipioResponse[]>([]);
  /** Otro cliente que ya usa este telefono o correo (solo aviso, no impide guardar). */
  readonly compartidoCon = signal<Partial<Record<'telefono' | 'correo', string>>>({});

  // Telefono y correo son opcionales por separado, pero el grupo exige al menos uno.
  readonly formulario = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    correo: ['', [Validators.email, Validators.maxLength(150)]],
    telefono: ['', [Validators.pattern(/^[0-9+\- ]{8,20}$/)]],
    // Vacio = CF (consumidor final). Se valida el digito verificador.
    nit: ['', [Validators.maxLength(20), nitValidator]],
    direccion: ['', [Validators.required, Validators.maxLength(255)]],
    // El departamento solo filtra la lista de municipios; lo que se guarda es el municipio.
    idDepartamento: this.fb.control<number | null>(null, Validators.required),
    idMunicipio: this.fb.control<number | null>(null, Validators.required),
  }, { validators: alMenosUnoValidator('telefono', 'correo') });

  /** Cuenta cuántos campos del formulario tienen Validators.required. */
  readonly camposRequeridos = computed(() => {
    const controles = this.formulario.controls;
    return Object.values(controles).filter((c) => c.hasValidator(Validators.required)).length;
  });

  ngOnInit(): void {
    this.departamentoService.listar().subscribe((d) => this.departamentos.set(d));

    // El aviso de telefono o correo compartido se borra en cuanto se cambia el dato.
    for (const campo of ['telefono', 'correo'] as const) {
      this.formulario.controls[campo].valueChanges.subscribe(() =>
        this.compartidoCon.update(({ [campo]: _, ...resto }) => resto),
      );
    }

    // Municipio bloqueado hasta que se elija departamento
    this.formulario.controls.idMunicipio.disable();

    // Al elegir departamento, limpia el municipio y carga solo los de ese departamento.
    this.formulario.controls.idDepartamento.valueChanges.subscribe((idDepartamento) => {
      this.formulario.controls.idMunicipio.setValue(null);
      this.municipios.set([]);
      if (idDepartamento) {
        this.municipioService.listar(idDepartamento).subscribe((m) => {
          this.municipios.set(m);
          this.formulario.controls.idMunicipio.enable();
        });
      } else {
        this.formulario.controls.idMunicipio.disable();
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
        this.formulario.controls.idMunicipio.enable();
        this.formulario.controls.idMunicipio.setValue(cliente.idMunicipio);
      });
    });
  }

  /** Al salir del campo, muestra el NIT como se va a guardar (ej. 67693598 -> 6769359-8). */
  formatearNit(): void {
    const control = this.formulario.controls.nit;
    const normalizado = normalizarNit(control.value);
    if (normalizado && control.value.trim()) {
      control.setValue(normalizado);
    }
  }

  /**
   * Al salir de NIT, telefono o correo se revisa si ya pertenece a otro cliente. Un NIT
   * repetido deja el campo en error con el nombre del dueño y no se puede guardar (el
   * backend lo vuelve a validar); el error se limpia solo al volver a escribir, porque
   * Angular recalcula los validadores del campo. Telefono y correo solo muestran un aviso.
   */
  verificarDuplicado(campo: CampoRevisado): void {
    const control = this.formulario.controls[campo];
    const valor = control.value?.trim();
    if (!valor || control.invalid || (campo === 'nit' && normalizarNit(valor) === 'CF')) {
      return;
    }
    this.clienteService.posiblesDuplicados({ [campo]: valor }, this.idCliente()).subscribe((parecidos) => {
      const dueno = parecidos.find((p) => p.coincidencias.includes(ETIQUETA_CAMPO[campo]));
      if (!dueno || control.value?.trim() !== valor) {
        return;
      }
      const nombre = dueno.activo ? dueno.nombre : `${dueno.nombre} (inactivo)`;
      if (campo === 'nit') {
        control.setErrors({ ...control.errors, duplicado: nombre });
        control.markAsTouched();
      } else {
        this.compartidoCon.update((actual) => ({ ...actual, [campo]: nombre }));
      }
    });
  }

  duplicadoDe(campo: 'nit'): string | null {
    return this.formulario.controls[campo].getError('duplicado') ?? null;
  }

  get faltaContacto(): boolean {
    const { telefono, correo } = this.formulario.controls;
    return this.formulario.hasError('contacto') && (telefono.touched || correo.touched);
  }

  guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    const v = this.formulario.getRawValue();
    // El NIT repetido bloquea el guardado y se marca en su campo. Telefono, correo o
    // nombre repetidos solo piden confirmar que se trata de otro cliente.
    this.clienteService
      .posiblesDuplicados({ nombre: v.nombre, nit: v.nit, telefono: v.telefono, correo: v.correo }, this.idCliente())
      .subscribe({
        next: (todos) => {
          const duenoNit = todos.find((p) => p.coincidencias.includes(ETIQUETA_CAMPO.nit));
          if (duenoNit) {
            const control = this.formulario.controls.nit;
            control.setErrors({ ...control.errors, duplicado: duenoNit.activo ? duenoNit.nombre : `${duenoNit.nombre} (inactivo)` });
            control.markAsTouched();
            this.guardando.set(false);
            return;
          }
          if (!todos.length) {
            this.enviar();
            return;
          }
          this.dialog
            .open(ConfirmDialog, { data: { titulo: 'Posible cliente duplicado', mensaje: this.mensajeDuplicados(todos) } })
            .afterClosed()
            .subscribe((confirmado) => (confirmado ? this.enviar() : this.guardando.set(false)));
        },
        error: () => this.guardando.set(false),
      });
  }

  private mensajeDuplicados(parecidos: PosibleDuplicado[]): string {
    const lineas = parecidos
      .slice(0, 3)
      .map((p) => `• ${p.nombre}${p.activo ? '' : ' (inactivo)'} — mismo ${p.coincidencias.join(', ')}`);
    const extra = parecidos.length > 3 ? `\n…y ${parecidos.length - 3} más.` : '';
    return `Ya hay clientes registrados con datos iguales:\n${lineas.join('\n')}${extra}\n\n¿Es un cliente distinto? Confirme para guardarlo.`;
  }

  private enviar(): void {
    const v = this.formulario.getRawValue();
    const request = {
      nombre: v.nombre,
      correo: v.correo || null,
      telefono: v.telefono || null,
      nit: v.nit || null,
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
