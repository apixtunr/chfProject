import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ClienteService } from '../cliente.service';

@Component({
  selector: 'app-cliente-form',
  imports: [ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './cliente-form.html',
  styleUrl: './cliente-form.scss',
})
export class ClienteForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly clienteService = inject(ClienteService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly idCliente = signal<number | null>(null);
  readonly guardando = signal(false);

  readonly formulario: ReturnType<typeof ClienteForm.prototype.crearFormulario>;

  constructor() {
    this.formulario = this.crearFormulario();
  }

  private crearFormulario() {
    return this.fb.nonNullable.group({
      nombre: ['', [Validators.required, Validators.maxLength(150)]],
      correo: ['', [Validators.email, Validators.maxLength(150)]],
      telefono: ['', [Validators.pattern(/^[0-9+\- ]{8,20}$/)]],
      nit: ['', [Validators.maxLength(20)]],
    });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      return;
    }
    const id = Number(idParam);
    this.idCliente.set(id);
    this.clienteService.obtener(id).subscribe((cliente) => {
      this.formulario.patchValue({
        nombre: cliente.nombre,
        correo: cliente.correo ?? '',
        telefono: cliente.telefono ?? '',
        nit: cliente.nit ?? '',
      });
    });
  }

  guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    const request = this.formulario.getRawValue();
    const id = this.idCliente();
    const operacion = id ? this.clienteService.actualizar(id, request) : this.clienteService.crear(request);

    operacion.subscribe({
      next: () => {
        this.snackBar.open(id ? 'Cliente actualizado' : 'Cliente creado', 'Cerrar', { duration: 3000 });
        this.router.navigateByUrl('/clientes');
      },
      error: () => this.guardando.set(false),
    });
  }
}
