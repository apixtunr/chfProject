import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, RouterLink } from '@angular/router';
import { UbicacionResponse } from '../../../core/catalogos/ubicacion';
import { UbicacionService } from '../../../core/catalogos/ubicacion.service';
import { ClienteResponse } from '../../clientes/dto/cliente';
import { ClienteService } from '../../clientes/cliente.service';
import { CotizacionResponse } from '../../cotizaciones/dto/cotizacion';
import { CotizacionService } from '../../cotizaciones/cotizacion.service';
import { TipoEventoResponse } from '../dto/evento';
import { EventoService } from '../evento.service';

@Component({
  selector: 'app-evento-form',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
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
  private readonly cotizacionService = inject(CotizacionService);
  private readonly clienteService = inject(ClienteService);
  private readonly ubicacionService = inject(UbicacionService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly tieneCotizacion = signal(true);
  readonly guardando = signal(false);
  readonly cotizacionesAceptadas = signal<CotizacionResponse[]>([]);
  readonly clientes = signal<ClienteResponse[]>([]);
  readonly tiposEvento = signal<TipoEventoResponse[]>([]);
  readonly ubicaciones = signal<UbicacionResponse[]>([]);

  readonly formulario = this.crearFormulario();

  private crearFormulario() {
    return this.fb.nonNullable.group({
      idCotizacionVersion: this.fb.control<number | null>(null),
      idCliente: this.fb.control<number | null>(null),
      idTipoEvento: this.fb.control<number | null>(null, Validators.required),
      idUbicacion: this.fb.control<number | null>(null, Validators.required),
      fechaEvento: this.fb.control<Date | null>(null, Validators.required),
      horaInicio: [''],
      horaFin: [''],
      cantidadPersonas: this.fb.control<number | null>(null),
      observaciones: [''],
    });
  }

  ngOnInit(): void {
    this.cotizacionService.listar(null, 0, 100).subscribe((page) => {
      this.cotizacionesAceptadas.set(page.content.filter((c) => c.ultimaVersionEstado === 'ACEPTADA'));
    });
    this.clienteService.listar('', 0, 200).subscribe((page) => this.clientes.set(page.content));
    this.eventoService.listarTiposEvento().subscribe((t) => this.tiposEvento.set(t));
    this.ubicacionService.listar(null).subscribe((u) => this.ubicaciones.set(u));
  }

  cambiarModo(tieneCotizacion: boolean): void {
    this.tieneCotizacion.set(tieneCotizacion);
    this.formulario.patchValue({ idCotizacionVersion: null, idCliente: null });
  }

  guardar(): void {
    const controlPrincipal = this.tieneCotizacion()
      ? this.formulario.controls.idCotizacionVersion
      : this.formulario.controls.idCliente;
    if (this.formulario.invalid || !controlPrincipal.value) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    const v = this.formulario.getRawValue();
    this.eventoService
      .crear({
        idCotizacionVersion: this.tieneCotizacion() ? v.idCotizacionVersion : null,
        idCliente: this.tieneCotizacion() ? null : v.idCliente,
        idTipoEvento: v.idTipoEvento!,
        idUbicacion: v.idUbicacion!,
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
  }

  private aFechaIso(fecha: Date): string {
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }
}
