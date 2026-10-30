import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { of } from 'rxjs';
import { map } from 'rxjs/operators';
import { CotizacionService } from '../../cotizaciones/cotizacion.service';
import { EventoService } from '../../eventos/evento.service';
import { EventoResponse } from '../../eventos/dto/evento';
import { PagoService } from '../pago.service';
import { ReciboPago } from '../recibo-pago';
import { MetodoPagoResponse } from '../dto/pago';

interface ResumenEvento {
  total: number;
  abonado: number;
  pendiente: number;
}

@Component({
  selector: 'app-pago-form',
  imports: [MatIconModule, 
    ReactiveFormsModule,
    RouterLink,
    DecimalPipe,
    MatDatepickerModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './pago-form.html',
  styleUrl: './pago-form.scss',
})
export class PagoForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly pagoService = inject(PagoService);
  private readonly eventoService = inject(EventoService);
  private readonly cotizacionService = inject(CotizacionService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly reciboPago = inject(ReciboPago);

  readonly idPago = signal<number | null>(null);
  readonly idEvento = signal<number | null>(null);
  readonly guardando = signal(false);
  readonly evento = signal<EventoResponse | null>(null);
  readonly metodos = signal<MetodoPagoResponse[]>([]);
  readonly resumenEvento = signal<ResumenEvento | null>(null);

  readonly formulario = this.fb.nonNullable.group({
    idMetodoPago: this.fb.control<number | null>(null, Validators.required),
    monto: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.01)]),
    referenciaTransaccion: this.fb.nonNullable.control({ value: '', disabled: true }, Validators.maxLength(100)),
    observaciones: this.fb.nonNullable.control('', Validators.maxLength(255)),
    fechaPago: this.fb.control<Date | null>(null, Validators.required),
  });

  private idCostoEventoActual: number | null = null;

  ngOnInit(): void {
    this.pagoService.listarMetodos().subscribe((m) => {
      this.metodos.set(m);
      this.ajustarValidacionReferencia();
    });

    this.formulario.controls.idMetodoPago.valueChanges.subscribe(() => this.ajustarValidacionReferencia());

    const idPagoParam = this.route.snapshot.paramMap.get('id');
    const idEventoParam = this.route.snapshot.paramMap.get('idEvento');

    if (idPagoParam) {
      const id = Number(idPagoParam);
      this.idPago.set(id);
      this.pagoService.obtener(id).subscribe((pago) => {
        this.idCostoEventoActual = pago.idCostoEvento;
        this.idEvento.set(pago.idEvento);
        this.cargarEventoYResumen(pago.idEvento);
        this.formulario.patchValue({
          idMetodoPago: pago.idMetodoPago,
          monto: pago.monto,
          referenciaTransaccion: pago.referenciaTransaccion ?? '',
          observaciones: pago.observaciones ?? '',
          fechaPago: new Date(pago.fechaPago),
        });
      });
    } else if (idEventoParam) {
      const idEvento = Number(idEventoParam);
      this.idEvento.set(idEvento);
      this.cargarEventoYResumen(idEvento);
    }
  }

  get metodoRequiereReferencia(): boolean {
    const idMetodo = this.formulario.controls.idMetodoPago.value;
    return this.metodos().find((m) => m.idMetodoPago === idMetodo)?.requiereReferencia ?? false;
  }

  readonly hoy = new Date();

  private ajustarValidacionReferencia(): void {
    const control = this.formulario.controls.referenciaTransaccion;
    if (this.metodoRequiereReferencia) {
      control.setValidators([Validators.required, Validators.maxLength(100)]);
      control.enable({ emitEvent: false });
    } else {
      control.setValidators([Validators.maxLength(100)]);
      control.setValue('', { emitEvent: false });
      control.disable({ emitEvent: false });
    }
    control.updateValueAndValidity({ emitEvent: false });
  }

  private cargarEventoYResumen(idEvento: number): void {
    this.eventoService.obtener(idEvento).subscribe((evento) => {
      this.evento.set(evento);
      const total$ = evento.idCotizacionVersion
        ? this.cotizacionService.obtenerVersion(evento.idCotizacionVersion).pipe(map((v) => v.montoTotal))
        : of(evento.montoMenu);

      total$.subscribe((total) => {
        this.pagoService.listar(idEvento, 0, 200).subscribe((pagina) => {
          const abonado = pagina.content
            .filter((p) => p.idCostoEvento === null && p.estadoNombre !== 'ANULADO')
            .reduce((acc, p) => acc + p.monto, 0);
          this.resumenEvento.set({ total, abonado, pendiente: total - abonado });
        });
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
      idEvento: this.idEvento()!,
      idMetodoPago: v.idMetodoPago!,
      monto: v.monto!,
      referenciaTransaccion: v.referenciaTransaccion.trim() || null,
      observaciones: v.observaciones.trim() || null,
      fechaPago: this.aFechaHoraIso(v.fechaPago),
      idCostoEvento: this.idCostoEventoActual,
    };

    const id = this.idPago();
    const operacion = id ? this.pagoService.actualizar(id, request) : this.pagoService.crear(request);

    operacion.subscribe({
      next: (pago) => {
        this.snackBar.open(id ? 'Pago actualizado' : 'Pago registrado', 'Cerrar', { duration: 3000 });
        this.router.navigate(['/pagos/evento', this.idEvento()]).then(() => {
          if (!id) {
            this.reciboPago.ver(pago.idPago);
          }
        });
      },
      error: () => this.guardando.set(false),
    });
  }

  private aFechaHoraIso(fecha: Date | null): string | null {
    if (!fecha) {
      return null;
    }
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}T00:00:00`;
  }
}
