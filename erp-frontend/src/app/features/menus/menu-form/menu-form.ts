import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EstadoResponse } from '../../../core/catalogos/estado';
import { EstadoService } from '../../../core/catalogos/estado.service';
import { MenuPlatoService } from '../menu-plato.service';

const TIPO_ESTADO_GENERAL = 'GENERAL';

@Component({
  selector: 'app-menu-form',
  imports: [ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  templateUrl: './menu-form.html',
  styleUrl: './menu-form.scss',
})
export class MenuForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly menuPlatoService = inject(MenuPlatoService);
  private readonly estadoService = inject(EstadoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly idMenu = signal<number | null>(null);
  readonly guardando = signal(false);
  readonly estados = signal<EstadoResponse[]>([]);

  readonly formulario = this.fb.nonNullable.group({
    nombreMenu: ['', [Validators.required, Validators.maxLength(120)]],
    idEstado: this.fb.control<number | null>(null, Validators.required),
  });

  ngOnInit(): void {
    this.estadoService.listarPorTipo(TIPO_ESTADO_GENERAL).subscribe((e) => this.estados.set(e));

    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      return;
    }
    const id = Number(idParam);
    this.idMenu.set(id);
    this.menuPlatoService.obtenerMenu(id).subscribe((menu) => {
      this.formulario.patchValue({ nombreMenu: menu.nombreMenu, idEstado: menu.idEstado });
    });
  }

  guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    const v = this.formulario.getRawValue();
    const request = { nombreMenu: v.nombreMenu.trim(), idEstado: v.idEstado! };

    const id = this.idMenu();
    const operacion = id ? this.menuPlatoService.actualizarMenu(id, request) : this.menuPlatoService.crearMenu(request);

    operacion.subscribe({
      next: (menu) => {
        this.snackBar.open(id ? 'Menu actualizado' : 'Menu creado. Ahora agrega sus platos', 'Cerrar', {
          duration: 3000,
        });
        // Al crear, va directo a componer el menu
        this.router.navigate(['/menus', menu.idMenu]);
      },
      error: () => this.guardando.set(false),
    });
  }
}
