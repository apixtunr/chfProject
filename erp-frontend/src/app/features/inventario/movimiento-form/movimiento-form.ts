import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, RouterLink } from '@angular/router';
import { InventarioService } from '../inventario.service';
import { ProductoResponse, TipoMovimiento } from '../dto/inventario';

@Component({
  selector: 'app-movimiento-form',
  imports: [ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  templateUrl: './movimiento-form.html',
  styleUrl: './movimiento-form.scss',
})
export class MovimientoForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly inventarioService = inject(InventarioService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly guardando = signal(false);
  readonly productos = signal<ProductoResponse[]>([]);

  readonly tipos: { valor: TipoMovimiento; etiqueta: string; ayuda: string }[] = [
    { valor: 'ENTRADA', etiqueta: 'Entrada', ayuda: 'Compra o devolucion: suma al stock' },
    { valor: 'SALIDA', etiqueta: 'Salida', ayuda: 'Consumo o perdida: resta del stock' },
    { valor: 'AJUSTE', etiqueta: 'Ajuste', ayuda: 'Correccion de conteo fisico: cantidad con signo (+/-)' },
  ];

  readonly formulario = this.fb.nonNullable.group({
    idProducto: this.fb.control<number | null>(null, Validators.required),
    tipoMovimiento: this.fb.nonNullable.control<TipoMovimiento>('ENTRADA', Validators.required),
    cantidad: this.fb.control<number | null>(null, Validators.required),
    descripcion: this.fb.nonNullable.control('', Validators.maxLength(255)),
  });

  ngOnInit(): void {
    this.inventarioService.listarProductos('', 0, 200).subscribe((p) => this.productos.set(p.content));
  }

  get ayudaTipo(): string {
    const tipo = this.formulario.controls.tipoMovimiento.value;
    return this.tipos.find((t) => t.valor === tipo)?.ayuda ?? '';
  }

  guardar(): void {
    const v = this.formulario.getRawValue();

    // ENTRADA/SALIDA exigen magnitud positiva; AJUSTE cualquier valor distinto de cero
    if (v.cantidad !== null) {
      if (v.tipoMovimiento !== 'AJUSTE' && v.cantidad <= 0) {
        this.formulario.controls.cantidad.setErrors({ positiva: true });
      } else if (v.tipoMovimiento === 'AJUSTE' && v.cantidad === 0) {
        this.formulario.controls.cantidad.setErrors({ cero: true });
      }
    }

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    this.inventarioService
      .registrarMovimiento({
        idProducto: v.idProducto!,
        tipoMovimiento: v.tipoMovimiento,
        cantidad: v.cantidad!,
        descripcion: v.descripcion.trim() || null,
        idEvento: null,
      })
      .subscribe({
        next: (mov) => {
          this.snackBar.open(`Movimiento registrado. Stock actual: ${mov.cantidadTotalActual}`, 'Cerrar', {
            duration: 4000,
          });
          this.router.navigateByUrl('/inventario/movimientos');
        },
        error: () => this.guardando.set(false),
      });
  }
}
