import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged, of, switchMap } from 'rxjs';
import { DepartamentoResponse } from '../../../core/catalogos/departamento';
import { DepartamentoService } from '../../../core/catalogos/departamento.service';
import { MunicipioResponse } from '../../../core/catalogos/municipio';
import { MunicipioService } from '../../../core/catalogos/municipio.service';
import { UbicacionService } from '../../../core/catalogos/ubicacion.service';
import { ClienteResponse } from '../../clientes/dto/cliente';
import { ClienteService } from '../../clientes/cliente.service';
import { CotizacionResponse } from '../../cotizaciones/dto/cotizacion';
import { TipoEventoResponse } from '../dto/evento';
import { EventoService } from '../evento.service';

@Component({
  selector: 'app-evento-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatAutocompleteModule,
    MatDatepickerModule,
    MatButtonModule,
    MatButtonToggleModule,
  ],
  templateUrl: './evento-form.html',
  styleUrl: './evento-form.scss',
})
export class EventoForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly eventoService = inject(EventoService);
  private readonly clienteService = inject(ClienteService);
  private readonly ubicacionService = inject(UbicacionService);
  private readonly departamentoService = inject(DepartamentoService);
  private readonly municipioService = inject(MunicipioService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly modoTocado = signal(false);
  readonly guardando = signal(false);
  readonly cotizacionesAceptadas = signal<CotizacionResponse[]>([]);
  readonly tiposEvento = signal<TipoEventoResponse[]>([]);
  readonly departamentos = signal<DepartamentoResponse[]>([]);
  readonly municipios = signal<MunicipioResponse[]>([]);

  /** Texto que el usuario escribe para buscar el cliente; separado del idCliente que en realidad se envia. */
  readonly busquedaCliente = new FormControl('', { nonNullable: true });
  readonly clientesFiltrados = signal<ClienteResponse[]>([]);

  readonly formulario = this.crearFormulario();

  private crearFormulario() {
    return this.fb.nonNullable.group({
      tieneCotizacion: this.fb.control<boolean | null>(null),
      idCotizacionVersion: this.fb.control<number | null>(null),
      idCliente: this.fb.control<number | null>(null),
      idTipoEvento: this.fb.control<number | null>(null, Validators.required),
      idDepartamento: this.fb.control<number | null>(null, Validators.required),
      idMunicipio: this.fb.control<number | null>(null, Validators.required),
      direccion: ['', [Validators.required, Validators.maxLength(255)]],
      fechaEvento: this.fb.control<Date | null>(null, Validators.required),
      horaInicio: [''],
      horaFin: [''],
      cantidadPersonas: this.fb.control<number | null>(null),
      observaciones: [''],
    });
  }

  ngOnInit(): void {
    this.eventoService.listarCotizacionesDisponibles().subscribe((cotizaciones) => this.cotizacionesAceptadas.set(cotizaciones));
    this.eventoService.listarTiposEvento().subscribe((t) => this.tiposEvento.set(t));
    this.departamentoService.listar().subscribe((d) => this.departamentos.set(d));

    this.formulario.controls.idDepartamento.valueChanges.subscribe((idDepartamento) => {
      this.formulario.controls.idMunicipio.setValue(null);
      this.municipios.set([]);
      if (idDepartamento) {
        this.municipioService.listar(idDepartamento).subscribe((m) => this.municipios.set(m));
      }
    });

    this.formulario.controls.tieneCotizacion.valueChanges.subscribe((tieneCotizacion) => {
      this.modoTocado.set(true);
      this.formulario.patchValue({ idCotizacionVersion: null, idCliente: null });
      this.busquedaCliente.setValue('', { emitEvent: false });
      this.clientesFiltrados.set([]);

      const conCotizacion = this.formulario.controls.idCotizacionVersion;
      const directo = this.formulario.controls.idCliente;
      conCotizacion.setValidators(tieneCotizacion === true ? Validators.required : null);
      directo.setValidators(tieneCotizacion === false ? Validators.required : null);
      conCotizacion.updateValueAndValidity();
      directo.updateValueAndValidity();
    });

    this.busquedaCliente.valueChanges.subscribe((texto) => {
      // Si el usuario edita el texto sin volver a elegir una opcion, el id queda invalido.
      this.formulario.controls.idCliente.setValue(null);
      // Al borrar el texto, no debe quedar ninguna sugerencia visible.
      if (!texto.trim()) {
        this.clientesFiltrados.set([]);
      }
    });

    this.busquedaCliente.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((texto) => {
          const valor = texto.trim();
          return valor ? this.clienteService.listar(valor, 0, 10) : of(null);
        }),
      )
      .subscribe((page) => this.clientesFiltrados.set(page?.content ?? []));
  }

  tieneCotizacion(): boolean | null {
    return this.formulario.controls.tieneCotizacion.value;
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
    if (this.tieneCotizacion() === null) {
      this.modoTocado.set(true);
      return;
    }
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      this.busquedaCliente.markAsTouched();
      return;
    }

    this.guardando.set(true);
    const v = this.formulario.getRawValue();

    // La ubicacion es propia de este evento: se crea primero, y luego se usa su id al crear el evento.
    this.ubicacionService.crear({ idCliente: null, idMunicipio: v.idMunicipio!, direccion: v.direccion }).subscribe({
      next: (ubicacion) => {
        this.eventoService
          .crear({
            idCotizacionVersion: this.tieneCotizacion() ? v.idCotizacionVersion : null,
            idCliente: this.tieneCotizacion() ? null : v.idCliente,
            idTipoEvento: v.idTipoEvento!,
            idUbicacion: ubicacion.idUbicacion,
            fechaEvento: this.aFechaIso(v.fechaEvento!),
            horaInicio: v.horaInicio || null,
            horaFin: v.horaFin || null,
            cantidadPersonas: v.cantidadPersonas,
            observaciones: v.observaciones || null,
          })
          .subscribe({
            next: (evento) => {
              this.snackBar.open('Evento creado', 'Cerrar', { duration: 3000 });
              this.router.navigateByUrl(`/eventos/${evento.idEvento}`);
            },
            error: () => this.guardando.set(false),
          });
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
