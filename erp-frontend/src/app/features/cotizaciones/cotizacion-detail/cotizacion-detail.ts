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
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import {
  CotizacionResponse,
  CotizacionVersionResponse,
  PERMITEN_VERSION_NUEVA,
  horarioServicio,
  textoVigencia,
} from '../dto/cotizacion';

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
  private readonly dialog = inject(MatDialog);

  readonly idCotizacion: number;
  readonly cotizacion = signal<CotizacionResponse | null>(null);
  readonly versiones = signal<CotizacionVersionResponse[]>([]);

  readonly columnas = ['numero', 'estado', 'monto', 'fecha', 'acciones'];

  constructor() {
    this.idCotizacion = Number(this.route.snapshot.paramMap.get('id'));
  }

  readonly horarioServicio = horarioServicio;
  readonly textoVigencia = textoVigencia;

  /** Los datos generales se corrigen mientras la ultima version sigue en borrador (CREADA). */
  get puedeEditarDatos(): boolean {
    return (
      this.authService.tienePermiso(PAGINA_URL, 'modificacion') && this.versiones()[0]?.estadoNombre === 'CREADA'
    );
  }

  get puedeCrear(): boolean {
    return this.authService.tienePermiso(PAGINA_URL, 'alta');
  }

  /** Version nueva si el cliente no acepto la ultima: enviada (pide cambios), rechazada o vencida. */
  get puedeCrearVersion(): boolean {
    const ultima = this.versiones()[0]?.estadoNombre;
    return this.puedeCrear && !!ultima && PERMITEN_VERSION_NUEVA.includes(ultima);
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cotizacionService.obtener(this.idCotizacion).subscribe((c) => this.cotizacion.set(c));
    this.cotizacionService.listarVersiones(this.idCotizacion).subscribe((v) => this.versiones.set(v));
  }

  nuevaVersion(): void {
    const ultima = this.versiones()[0];
    if (ultima?.estadoNombre !== 'ENVIADA') {
      this.crearVersion();
      return;
    }
    // La enviada deja de valer: que quede claro antes de hacerlo.
    const ref = this.dialog.open(ConfirmDialog, {
      data: {
        titulo: 'Nueva versión',
        mensaje: `La versión ${ultima.numeroVersion} quedará como REEMPLAZADA y el cliente ya no podrá aceptarla. ¿Continuar?`,
      },
    });
    ref.afterClosed().subscribe((confirmado) => {
      if (confirmado) {
        this.crearVersion();
      }
    });
  }

  private crearVersion(): void {
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
