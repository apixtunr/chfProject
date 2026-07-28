import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { EstadoResponse } from '../../../core/catalogos/estado';
import { EstadoService } from '../../../core/catalogos/estado.service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { PagoService } from '../pago.service';
import { ComprobantePagoResponse, PagoResponse } from '../dto/pago';

const PAGINA_URL = '/api/pagos';
const TIPO_ESTADO_PAGO = 'PAGO';

@Component({
  selector: 'app-pago-detail',
  imports: [
    RouterLink,
    DatePipe,
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './pago-detail.html',
  styleUrl: './pago-detail.scss',
})
export class PagoDetail implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly pagoService = inject(PagoService);
  private readonly estadoService = inject(EstadoService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly pago = signal<PagoResponse | null>(null);
  readonly comprobantes = signal<ComprobantePagoResponse[]>([]);
  readonly estados = signal<EstadoResponse[]>([]);
  readonly agregandoComprobante = signal(false);

  readonly puedeEditar: boolean;
  readonly puedeAgregar: boolean;
  readonly puedeEliminar: boolean;

  readonly columnasComprobantes = ['numeroComprobante', 'tipoComprobante', 'fechaEmision', 'esValido', 'acciones'];

  readonly formularioComprobante = this.fb.nonNullable.group({
    numeroComprobante: ['', [Validators.required, Validators.maxLength(50)]],
    tipoComprobante: this.fb.nonNullable.control('', Validators.maxLength(50)),
    fechaEmision: this.fb.control<string | null>(null),
  });

  private idPago!: number;

  constructor() {
    this.puedeEditar = this.authService.tienePermiso(PAGINA_URL, 'modificacion');
    this.puedeAgregar = this.authService.tienePermiso(PAGINA_URL, 'alta');
    this.puedeEliminar = this.authService.tienePermiso(PAGINA_URL, 'baja');
  }

  ngOnInit(): void {
    this.idPago = Number(this.route.snapshot.paramMap.get('id'));
    this.estadoService.listarPorTipo(TIPO_ESTADO_PAGO).subscribe((e) => this.estados.set(e));
    this.cargar();
  }

  cargar(): void {
    this.pagoService.obtener(this.idPago).subscribe((p) => this.pago.set(p));
    this.pagoService.listarComprobantes(this.idPago).subscribe((c) => this.comprobantes.set(c));
  }

  cambiarEstado(idEstado: number): void {
    if (idEstado === this.pago()?.idEstado) {
      return;
    }
    this.pagoService.cambiarEstado(this.idPago, idEstado).subscribe((p) => {
      this.pago.set(p);
      this.snackBar.open(`Estado cambiado a ${p.estadoNombre}`, 'Cerrar', { duration: 3000 });
    });
  }

  agregarComprobante(): void {
    if (this.formularioComprobante.invalid) {
      this.formularioComprobante.markAllAsTouched();
      return;
    }
    const v = this.formularioComprobante.getRawValue();
    this.pagoService
      .agregarComprobante(this.idPago, {
        numeroComprobante: v.numeroComprobante.trim(),
        tipoComprobante: v.tipoComprobante.trim() || null,
        fechaEmision: v.fechaEmision,
        archivoUrl: null,
        esValido: true,
      })
      .subscribe(() => {
        this.snackBar.open('Comprobante agregado', 'Cerrar', { duration: 3000 });
        this.formularioComprobante.reset();
        this.agregandoComprobante.set(false);
        this.cargar();
      });
  }

  eliminarComprobante(comprobante: ComprobantePagoResponse): void {
    const ref = this.dialog.open(ConfirmDialog, {
      data: { titulo: 'Eliminar comprobante', mensaje: `¿Eliminar el comprobante "${comprobante.numeroComprobante}"?` },
    });

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.pagoService.eliminarComprobante(this.idPago, comprobante.idComprobante).subscribe(() => {
        this.snackBar.open('Comprobante eliminado', 'Cerrar', { duration: 3000 });
        this.cargar();
      });
    });
  }
}
