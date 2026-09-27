import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged, of, switchMap } from 'rxjs';
import { DepartamentoResponse } from '../../../core/catalogos/departamento';
import { DepartamentoService } from '../../../core/catalogos/departamento.service';
import { MunicipioResponse } from '../../../core/catalogos/municipio';
import { MunicipioService } from '../../../core/catalogos/municipio.service';
import { MINIMO_PERSONAS } from '../../../core/catalogos/menu';
import { TipoEventoResponse } from '../../../core/catalogos/tipo-evento';
import { TipoEventoService } from '../../../core/catalogos/tipo-evento.service';
import { UbicacionService } from '../../../core/catalogos/ubicacion.service';
import { ClienteResponse } from '../../clientes/dto/cliente';
import { ClienteService } from '../../clientes/cliente.service';
import { CotizacionService } from '../cotizacion.service';
import { HORAS_DE_INICIO, horarioServicio } from '../dto/cotizacion';

@Component({
  selector: 'app-cotizacion-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatIconModule,
    MatSelectModule,
    MatAutocompleteModule,
    MatDatepickerModule,
    MatButtonModule,
  ],
  templateUrl: './cotizacion-form.html',
  styleUrl: './cotizacion-form.scss',
})
export class CotizacionForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly cotizacionService = inject(CotizacionService);
  private readonly clienteService = inject(ClienteService);
  private readonly ubicacionService = inject(UbicacionService);
  private readonly departamentoService = inject(DepartamentoService);
  private readonly municipioService = inject(MunicipioService);
  private readonly tipoEventoService = inject(TipoEventoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);

  /** Texto que el usuario escribe para buscar; separado del idCliente que en realidad se envia. */
  readonly busquedaCliente = new FormControl('', { nonNullable: true });
  readonly clientesFiltrados = signal<ClienteResponse[]>([]);

  readonly tiposEvento = signal<TipoEventoResponse[]>([]);
  readonly departamentos = signal<DepartamentoResponse[]>([]);
  readonly municipios = signal<MunicipioResponse[]>([]);

  readonly guardando = signal(false);
  /** En edicion: la cotizacion y su ubicacion (la direccion se corrige sobre la misma). */
  readonly idCotizacion = signal<number | null>(null);
  private idUbicacion: number | null = null;
  /** Municipio a elegir en cuanto llegue la lista del departamento (al cargar para editar). */
  private municipioPendiente: number | null = null;

  readonly horasDeInicio = HORAS_DE_INICIO;
  readonly horarioServicio = horarioServicio;
  readonly minimoPersonas = MINIMO_PERSONAS;
  /** El evento no puede ser en el pasado (el backend lo vuelve a validar). */
  readonly hoy = new Date(new Date().setHours(0, 0, 0, 0));

  readonly formulario = this.crearFormulario();

  private crearFormulario() {
    return this.fb.nonNullable.group({
      idCliente: this.fb.control<number | null>(null, Validators.required),
      idTipoEvento: this.fb.control<number | null>(null, Validators.required),
      idDepartamento: this.fb.control<number | null>(null, Validators.required),
      idMunicipio: this.fb.control<number | null>(null, Validators.required),
      direccion: ['', [Validators.required, Validators.maxLength(255)]],
      cantidadPersonas: this.fb.control<number | null>(null, [Validators.required, Validators.min(1)]),
      fechaEvento: this.fb.control<Date | null>(null, Validators.required),
      presupuestoCliente: this.fb.control<number | null>(null),
      horaInicio: this.fb.control<string | null>(null, Validators.required),
    });
  }

  ngOnInit(): void {
    this.tipoEventoService.listar().subscribe((t) => this.tiposEvento.set(t));
    const idParam = this.route.snapshot.paramMap.get('id');
    this.departamentoService.listar().subscribe((d) => {
      this.departamentos.set(d);
      // La ubicacion guardada trae el nombre del departamento: hace falta la lista para elegirlo.
      if (idParam) {
        this.cargarParaEditar(Number(idParam));
      }
    });

    this.formulario.controls.idDepartamento.valueChanges.subscribe((idDepartamento) => {
      this.formulario.controls.idMunicipio.setValue(null);
      this.municipios.set([]);
      if (idDepartamento) {
        this.municipioService.listar(idDepartamento).subscribe((m) => {
          this.municipios.set(m);
          if (this.municipioPendiente) {
            this.formulario.controls.idMunicipio.setValue(this.municipioPendiente);
            this.municipioPendiente = null;
          }
        });
      }
    });

    this.busquedaCliente.valueChanges.subscribe((texto) => {
      // Si el usuario edita el texto sin volver a elegir una opcion, el id queda invalido.
      this.formulario.controls.idCliente.setValue(null);
      // Al seleccionar una opcion, el autocomplete guarda el ClienteResponse completo
      // (no un string) como valor del control; solo hay texto libre que evaluar cuando
      // sigue siendo un string.
      if (typeof texto === 'string' && !texto.trim()) {
        this.clientesFiltrados.set([]);
      }
    });

    this.busquedaCliente.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((texto) => {
          const valor = typeof texto === 'string' ? texto.trim() : '';
          return valor ? this.clienteService.listar(valor, 0, 10, 'ACTIVO') : of(null);
        }),
      )
      .subscribe((page) => this.clientesFiltrados.set(page?.content ?? []));
  }

  /** Solo avisa: la empresa atiende desde 50 personas, pero la decision es de quien cotiza. */
  debajoDelMinimo(): boolean {
    const personas = this.formulario.controls.cantidadPersonas.value;
    return personas != null && personas > 0 && personas < MINIMO_PERSONAS;
  }

  /**
   * Edicion de los datos generales. Solo se llega aqui con la ultima version en CREADA:
   * una vez enviada, el backend ya no deja cambiarlos (hay que crear una version nueva).
   */
  private cargarParaEditar(id: number): void {
    this.idCotizacion.set(id);
    this.cotizacionService.obtener(id).subscribe((c) => {
      this.idUbicacion = c.idUbicacion;
      this.formulario.patchValue({
        idCliente: c.idCliente,
        idTipoEvento: c.idTipoEvento,
        cantidadPersonas: c.cantidadPersonas,
        fechaEvento: c.fechaEvento ? this.deFechaIso(c.fechaEvento) : null,
        presupuestoCliente: c.presupuestoCliente,
        horaInicio: c.horaInicio ? c.horaInicio.substring(0, 5) : null,
      });
      this.clienteService.obtener(c.idCliente).subscribe((cliente) => {
        // Sin emitir: el cambio de texto borraria el idCliente que se acaba de poner.
        this.busquedaCliente.setValue(cliente as never, { emitEvent: false });
      });
      this.ubicacionService.obtener(c.idUbicacion).subscribe((u) => {
        this.formulario.controls.direccion.setValue(u.direccion);
        this.municipioPendiente = u.idMunicipio;
        const departamento = this.departamentos().find((d) => d.nombreDepartamento === u.departamentoNombre);
        this.formulario.controls.idDepartamento.setValue(departamento?.idDepartamento ?? null);
      });
    });
  }

  mostrarCliente(cliente: ClienteResponse | string | null): string {
    if (!cliente || typeof cliente === 'string') {
      return '';
    }
    return cliente.nombre;
  }

  seleccionarCliente(event: MatAutocompleteSelectedEvent): void {
    const cliente = event.option.value as ClienteResponse;
    this.formulario.controls.idCliente.setValue(cliente.idCliente);
  }

  guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      this.busquedaCliente.markAsTouched();
      return;
    }

    this.guardando.set(true);
    const v = this.formulario.getRawValue();
    const datos = {
      idCliente: v.idCliente!,
      idTipoEvento: v.idTipoEvento!,
      cantidadPersonas: v.cantidadPersonas!,
      fechaEvento: v.fechaEvento ? this.aFechaIso(v.fechaEvento) : null,
      presupuestoCliente: v.presupuestoCliente,
      horaInicio: v.horaInicio,
    };
    const ubicacion = { idMunicipio: v.idMunicipio!, direccion: v.direccion };

    const idCotizacion = this.idCotizacion();
    if (idCotizacion && this.idUbicacion) {
      const idUbicacion = this.idUbicacion;
      this.ubicacionService.actualizar(idUbicacion, ubicacion).subscribe({
        next: () =>
          this.cotizacionService.actualizar(idCotizacion, { ...datos, idUbicacion }).subscribe({
            next: () => {
              this.snackBar.open('Cotizacion actualizada', 'Cerrar', { duration: 3000 });
              this.router.navigateByUrl(`/cotizaciones/${idCotizacion}`);
            },
            error: () => this.guardando.set(false),
          }),
        error: () => this.guardando.set(false),
      });
      return;
    }

    // La ubicacion es propia de esta cotizacion: se crea primero, y luego se usa su id.
    this.ubicacionService.crear(ubicacion).subscribe({
      next: (nueva) => {
        this.cotizacionService
          .crear({ ...datos, idUbicacion: nueva.idUbicacion })
          .subscribe({
            next: (cotizacion) => {
              this.snackBar.open('Cotizacion creada', 'Cerrar', { duration: 3000 });
              this.router.navigateByUrl(`/cotizaciones/${cotizacion.idCotizacion}`);
            },
            error: () => this.guardando.set(false),
          });
      },
      error: () => this.guardando.set(false),
    });
  }

  /** "2026-12-05" -> fecha local (sin pasar por UTC, que la correria un dia). */
  private deFechaIso(fecha: string): Date {
    const [anio, mes, dia] = fecha.split('-').map(Number);
    return new Date(anio, mes - 1, dia);
  }

  private aFechaIso(fecha: Date): string {
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }
}
