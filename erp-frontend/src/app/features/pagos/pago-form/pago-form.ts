import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EventoService } from '../../eventos/evento.service';
import { EventoResponse } from '../../eventos/dto/evento';
import { PagoService } from '../pago.service';
import { MetodoPagoResponse } from '../dto/pago';

@Component({
  selector: 'app-pago-form',
  imports: [ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  templateUrl: './pago-form.html',
  styleUrl: './pago-form.scss',
})
export class PagoForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly pagoService = inject(PagoService);
  private readonly eventoService = inject(EventoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly idPago = signal<number | null>(null);
  readonly guardando = signal(false);
  readonly eventos = signal<EventoResponse[]>([]);
  readonly metodos = signal<MetodoPagoResponse[]>([]);

  readonly formulario = this.fb.nonNullable.group({
    idEvento: this.fb.control<number | null>(null, Validators.required),
    idMetodoPago: this.fb.control<number | null>(null, Validators.required),
    monto: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.01)]),
    referenciaTransaccion: this.fb.nonNullable.control('', Validators.maxLength(100)),
    observaciones: this.fb.nonNullable.control('', Validators.maxLength(255)),
  });

  ngOnInit(): void {
    this.eventoService.listar(null, null, 0, 200).subscribe((p) => this.eventos.set(p.content));
    this.pagoService.listarMetodos().subscribe((m) => this.metodos.set(m));

    // La referencia es obligatoria solo si el metodo la requiere
    this.formulario.controls.idMetodoPago.valueChanges.subscribe(() => this.ajustarValidacionReferencia());

    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      return;
    }
    const id = Number(idParam);
    this.idPago.set(id);
    this.pagoService.obtener(id).subscribe((pago) => {
      this.formulario.patchValue({
        idEvento: pago.idEvento,
        idMetodoPago: pago.idMetodoPago,
        monto: pago.monto,
        referenciaTransaccion: pago.referenciaTransaccion ?? '',
        observaciones: pago.observaciones ?? '',
      });
    });
  }

  get metodoRequiereReferencia(): boolean {
    const idMetodo = this.formulario.controls.idMetodoPago.value;
    return this.metodos().find((m) => m.idMetodoPago === idMetodo)?.requiereReferencia ?? false;
  }

  private ajustarValidacionReferencia(): void {
    const control = this.formulario.controls.referenciaTransaccion;
    control.setValidators(
      this.metodoRequiereReferencia
        ? [Validators.required, Validators.maxLength(100)]
        : [Validators.maxLength(100)],
    );
    control.updateValueAndValidity();
  }

  etiquetaEvento(e: EventoResponse): string {
    return `#${e.idEvento} · ${e.clienteNombre} · ${e.fechaEvento}`;
  }

  guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    const v = this.formulario.getRawValue();
    const request = {
      idEvento: v.idEvento!,
      idMetodoPago: v.idMetodoPago!,
      monto: v.monto!,
      referenciaTransaccion: v.referenciaTransaccion.trim() || null,
      observaciones: v.observaciones.trim() || null,
      fechaPago: null,
    };

    const id = this.idPago();
    const operacion = id ? this.pagoService.actualizar(id, request) : this.pagoService.crear(request);

    operacion.subscribe({
      next: () => {
        this.snackBar.open(id ? 'Pago actualizado' : 'Pago registrado', 'Cerrar', { duration: 3000 });
        this.router.navigateByUrl('/pagos');
      },
      error: () => this.guardando.set(false),
    });
  }
}
