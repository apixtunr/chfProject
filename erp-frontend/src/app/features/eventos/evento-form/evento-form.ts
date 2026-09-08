import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
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
    DatePipe,
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
  // Horarios de servicio del negocio: cada hora de inicio dura 4 horas, salvo
  // que exceda las 10:00 pm, en cuyo caso se recorta a esa hora (excepcion
  // vigente solo para el arranque de las 6:00 pm y 7:00 pm).
  private static readonly HORA_INICIO_MINIMA = 11;
  private static readonly HORA_INICIO_MAXIMA = 19;
  private static readonly HORA_FIN_TOPE = 22;
  private static readonly DURACION_HORAS = 4;

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
  readonly idCotizacionVersionSeleccionada = signal<number | null>(null);
  readonly cotizacionSeleccionada = computed(() =>
    this.cotizacionesAceptadas().find((c) => c.ultimaVersionId === this.idCotizacionVersionSeleccionada()) ?? null,
  );
  readonly tiposEvento = signal<TipoEventoResponse[]>([]);
  readonly departamentos = signal<DepartamentoResponse[]>([]);
  readonly municipios = signal<MunicipioResponse[]>([]);

  /** Texto que el usuario escribe para buscar el cliente; separado del idCliente que en realidad se envia. */
  readonly busquedaCliente = new FormControl('', { nonNullable: true });
  readonly clientesFiltrados = signal<ClienteResponse[]>([]);

  readonly formulario = this.crearFormulario();

  /** Franjas de horario validas segun los horarios de servicio del negocio. */
  readonly horarios: { horaInicio: Date; label: string }[] = this.crearHorarios();

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
      horaInicio: this.fb.control<Date | null>(null),
      horaFin: this.fb.control<Date | null>(null),
      cantidadPersonas: this.fb.control<number | null>(null),
      observaciones: [''],
    });
  }

  ngOnInit(): void {
    this.eventoService.listarCotizacionesDisponibles().subscribe((cotizaciones) => this.cotizacionesAceptadas.set(cotizaciones));
    this.eventoService.listarTiposEvento().subscribe((t) => this.tiposEvento.set(t));
    this.departamentoService.listar().subscribe((d) => this.departamentos.set(d));

    // La hora fin la calcula el sistema segun el horario de servicio: el usuario
    // no la edita directamente.
    this.formulario.controls.horaFin.disable();
    this.formulario.controls.horaInicio.valueChanges.subscribe((horaInicio) => {
      this.formulario.controls.horaFin.setValue(this.calcularHoraFin(horaInicio), { emitEvent: false });
    });

    this.formulario.controls.idCotizacionVersion.valueChanges.subscribe((idCotizacionVersion) => {
      this.idCotizacionVersionSeleccionada.set(idCotizacionVersion);
    });

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

      // Si viene de una cotizacion, el tipo/ubicacion/fecha/personas ya se aceptaron ahi:
      // no se vuelven a pedir en este formulario.
      const esDirecto = tieneCotizacion === false;
      const camposSimples = [
        this.formulario.controls.idTipoEvento,
        this.formulario.controls.idDepartamento,
        this.formulario.controls.idMunicipio,
        this.formulario.controls.fechaEvento,
      ];
      for (const campo of camposSimples) {
        campo.setValidators(esDirecto ? Validators.required : null);
        campo.updateValueAndValidity();
      }
      this.formulario.controls.direccion.setValidators(
        esDirecto ? [Validators.required, Validators.maxLength(255)] : [Validators.maxLength(255)],
      );
      this.formulario.controls.direccion.updateValueAndValidity();
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

    if (this.tieneCotizacion()) {
      // Tipo de evento, ubicacion, fecha y cantidad de personas ya se aceptaron en la
      // cotizacion: el backend los toma de ahi, no se vuelven a pedir ni a enviar.
      this.eventoService
        .crear({
          idCotizacionVersion: v.idCotizacionVersion,
          idCliente: null,
          idTipoEvento: null,
          idUbicacion: null,
          fechaEvento: null,
          horaInicio: this.aHoraString(v.horaInicio),
          horaFin: this.aHoraString(v.horaFin),
          cantidadPersonas: null,
          observaciones: v.observaciones || null,
        })
        .subscribe({
          next: (evento) => {
            this.snackBar.open('Evento creado', 'Cerrar', { duration: 3000 });
            this.router.navigateByUrl(`/eventos/${evento.idEvento}`);
          },
          error: () => this.guardando.set(false),
        });
      return;
    }

    // La ubicacion es propia de este evento: se crea primero, y luego se usa su id al crear el evento.
    this.ubicacionService.crear({ idMunicipio: v.idMunicipio!, direccion: v.direccion }).subscribe({
      next: (ubicacion) => {
        this.eventoService
          .crear({
            idCotizacionVersion: null,
            idCliente: v.idCliente,
            idTipoEvento: v.idTipoEvento!,
            idUbicacion: ubicacion.idUbicacion,
            fechaEvento: this.aFechaIso(v.fechaEvento!),
            horaInicio: this.aHoraString(v.horaInicio),
            horaFin: this.aHoraString(v.horaFin),
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

  private crearHorarios(): { horaInicio: Date; label: string }[] {
    const horarios: { horaInicio: Date; label: string }[] = [];
    for (let hora = EventoForm.HORA_INICIO_MINIMA; hora <= EventoForm.HORA_INICIO_MAXIMA; hora++) {
      const inicio = new Date();
      inicio.setHours(hora, 0, 0, 0);
      const fin = this.calcularHoraFin(inicio)!;
      horarios.push({ horaInicio: inicio, label: `${this.formatearHora24(inicio)} a ${this.formatearHora24(fin)}` });
    }
    return horarios;
  }

  private calcularHoraFin(horaInicio: Date | null): Date | null {
    if (!horaInicio) {
      return null;
    }
    const horaFin = new Date(horaInicio);
    const horaFinCalculada = Math.min(horaInicio.getHours() + EventoForm.DURACION_HORAS, EventoForm.HORA_FIN_TOPE);
    horaFin.setHours(horaFinCalculada, 0, 0, 0);
    return horaFin;
  }

  private formatearHora24(fecha: Date): string {
    return `${String(fecha.getHours()).padStart(2, '0')}:00`;
  }

  private aHoraString(hora: Date | null): string | null {
    if (!hora) {
      return null;
    }
    const horas = String(hora.getHours()).padStart(2, '0');
    const minutos = String(hora.getMinutes()).padStart(2, '0');
    return `${horas}:${minutos}`;
  }
}
