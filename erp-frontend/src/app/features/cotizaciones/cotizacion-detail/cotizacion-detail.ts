import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { CotizacionService } from '../cotizacion.service';
import { CotizacionResponse, CotizacionVersionResponse } from '../dto/cotizacion';

const PAGINA_URL = '/api/cotizaciones';

@Component({
  selector: 'app-cotizacion-detail',
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTooltipModule,
  ],
  templateUrl: './cotizacion-detail.html',
  styleUrl: './cotizacion-detail.scss',
})
export class CotizacionDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly cotizacionService = inject(CotizacionService);
  private readonly authService = inject(AuthService);
  private readonly snackBar = inject(MatSnackBar);

  readonly idCotizacion: number;
  readonly cotizacion = signal<CotizacionResponse | null>(null);
  readonly versiones = signal<CotizacionVersionResponse[]>([]);

  readonly columnas = ['numero', 'estado', 'monto', 'fecha', 'acciones'];

  constructor() {
    this.idCotizacion = Number(this.route.snapshot.paramMap.get('id'));
  }

  get puedeCrear(): boolean {
    return this.authService.tienePermiso(PAGINA_URL, 'alta');
  }

  /** Solo se puede crear una version nueva cuando la ultima quedo RECHAZADA. */
  get puedeCrearVersion(): boolean {
    return this.puedeCrear && this.versiones()[0]?.estadoNombre === 'RECHAZADA';
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cotizacionService.obtener(this.idCotizacion).subscribe((c) => this.cotizacion.set(c));
    this.cotizacionService.listarVersiones(this.idCotizacion).subscribe((v) => this.versiones.set(v));
  }

  nuevaVersion(): void {
    this.cotizacionService.crearVersion(this.idCotizacion, true).subscribe(() => {
      this.snackBar.open('Version creada', 'Cerrar', { duration: 3000 });
      this.cargar();
    });
  }

  colorEstado(estado: string): string {
    switch (estado) {
      case 'ACEPTADA':
        return 'primary';
      case 'RECHAZADA':
        return 'warn';
      default:
        return '';
    }
  }
}
