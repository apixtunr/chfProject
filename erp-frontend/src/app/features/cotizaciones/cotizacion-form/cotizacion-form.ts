import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, RouterLink } from '@angular/router';
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
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly clientes = signal<ClienteResponse[]>([]);
  readonly guardando = signal(false);

  readonly formulario = this.crearFormulario();

  private crearFormulario() {
    return this.fb.nonNullable.group({
      idCliente: this.fb.control<number | null>(null, Validators.required),
      fechaEvento: this.fb.control<Date | null>(null),
      presupuestoCliente: this.fb.control<number | null>(null),
    });
  }

  ngOnInit(): void {
    this.clienteService.listar('', 0, 200).subscribe((page) => this.clientes.set(page.content));
  }

  guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    const valores = this.formulario.getRawValue();
    this.cotizacionService
      .crear({
        idCliente: valores.idCliente!,
        fechaEvento: valores.fechaEvento ? this.aFechaIso(valores.fechaEvento) : null,
        presupuestoCliente: valores.presupuestoCliente,
      })
      .subscribe({
        next: (cotizacion) => {
          this.snackBar.open('Cotizacion creada', 'Cerrar', { duration: 3000 });
          this.router.navigateByUrl(`/cotizaciones/${cotizacion.idCotizacion}`);
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
