import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
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
import { TipoEventoResponse } from '../../../core/catalogos/tipo-evento';
import { TipoEventoService } from '../../../core/catalogos/tipo-evento.service';
import { UbicacionService } from '../../../core/catalogos/ubicacion.service';
import { ClienteResponse } from '../../clientes/dto/cliente';
import { ClienteService } from '../../clientes/cliente.service';
import { CotizacionService } from '../cotizacion.service';

@Component({
  selector: 'app-cotizacion-form',
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
  private readonly snackBar = inject(MatSnackBar);

  /** Texto que el usuario escribe para buscar; separado del idCliente que en realidad se envia. */
  readonly busquedaCliente = new FormControl('', { nonNullable: true });
  readonly clientesFiltrados = signal<ClienteResponse[]>([]);

  readonly tiposEvento = signal<TipoEventoResponse[]>([]);
  readonly departamentos = signal<DepartamentoResponse[]>([]);
  readonly municipios = signal<MunicipioResponse[]>([]);

  readonly guardando = signal(false);

  readonly formulario = this.crearFormulario();

  private crearFormulario() {
    return this.fb.nonNullable.group({
      idCliente: this.fb.control<number | null>(null, Validators.required),
      idTipoEvento: this.fb.control<number | null>(null, Validators.required),
      idDepartamento: this.fb.control<number | null>(null, Validators.required),
      idMunicipio: this.fb.control<number | null>(null, Validators.required),
      direccion: ['', [Validators.required, Validators.maxLength(255)]],
      cantidadPersonas: this.fb.control<number | null>(null, [Validators.required, Validators.min(1)]),
      fechaEvento: this.fb.control<Date | null>(null),
      presupuestoCliente: this.fb.control<number | null>(null),
    });
  }

  ngOnInit(): void {
    this.tipoEventoService.listar().subscribe((t) => this.tiposEvento.set(t));
    this.departamentoService.listar().subscribe((d) => this.departamentos.set(d));

    this.formulario.controls.idDepartamento.valueChanges.subscribe((idDepartamento) => {
      this.formulario.controls.idMunicipio.setValue(null);
      this.municipios.set([]);
      if (idDepartamento) {
        this.municipioService.listar(idDepartamento).subscribe((m) => this.municipios.set(m));
      }
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

    // La ubicacion es propia de esta cotizacion: se crea primero, y luego se usa su id.
    this.ubicacionService.crear({ idMunicipio: v.idMunicipio!, direccion: v.direccion }).subscribe({
      next: (ubicacion) => {
        this.cotizacionService
          .crear({
            idCliente: v.idCliente!,
            idTipoEvento: v.idTipoEvento!,
            idUbicacion: ubicacion.idUbicacion,
            cantidadPersonas: v.cantidadPersonas!,
            fechaEvento: v.fechaEvento ? this.aFechaIso(v.fechaEvento) : null,
            presupuestoCliente: v.presupuestoCliente,
          })
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

  private aFechaIso(fecha: Date): string {
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }
}
