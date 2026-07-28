import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CategoriaProductoResponse } from '../dto/inventario';
import { InventarioService } from '../inventario.service';

@Component({
  selector: 'app-producto-form',
  imports: [ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  templateUrl: './producto-form.html',
  styleUrl: './producto-form.scss',
})
export class ProductoForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly inventarioService = inject(InventarioService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly idProducto = signal<number | null>(null);
  readonly guardando = signal(false);
  readonly categorias = signal<CategoriaProductoResponse[]>([]);

  readonly formulario = this.fb.nonNullable.group({
    nombreProducto: ['', [Validators.required, Validators.maxLength(120)]],
    idCategoria: this.fb.control<number | null>(null, Validators.required),
    unidadMedida: ['', [Validators.required, Validators.maxLength(30)]],
    precioUnitario: this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]),
  });

  ngOnInit(): void {
    this.inventarioService.listarCategorias().subscribe((c) => this.categorias.set(c));

    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      return;
    }
    const id = Number(idParam);
    this.idProducto.set(id);
    this.inventarioService.obtenerProducto(id).subscribe((producto) => {
      this.formulario.patchValue({
        nombreProducto: producto.nombreProducto,
        idCategoria: producto.idCategoria,
        unidadMedida: producto.unidadMedida,
        precioUnitario: producto.precioUnitario,
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
      nombreProducto: v.nombreProducto.trim(),
      idCategoria: v.idCategoria!,
      unidadMedida: v.unidadMedida.trim(),
      precioUnitario: v.precioUnitario!,
    };

    const id = this.idProducto();
    const operacion = id
      ? this.inventarioService.actualizarProducto(id, request)
      : this.inventarioService.crearProducto(request);

    operacion.subscribe({
      next: () => {
        this.snackBar.open(id ? 'Producto actualizado' : 'Producto creado', 'Cerrar', { duration: 3000 });
        this.router.navigateByUrl('/inventario/productos');
      },
      error: () => this.guardando.set(false),
    });
  }
}
