import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { EstadoResponse } from '../../../core/catalogos/estado';
import { EstadoService } from '../../../core/catalogos/estado.service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { VisorArchivo } from '../../../shared/visor-archivo/visor-archivo';
import { comprimirImagen, tamanoLegible } from '../../../shared/comprimir-imagen';
import { PagoService } from '../pago.service';
import { ReciboPago } from '../recibo-pago';
import {
  ARCHIVOS_COMPROBANTE,
  ComprobantePagoResponse,
  PagoResponse,
  TAMANO_MAXIMO_COMPROBANTE,
  TIPOS_COMPROBANTE,
} from '../dto/pago';
import { TablaResponsiva } from '../../../shared/tabla-responsiva';
import { SelectBuscable } from '../../../shared/select-buscable';

const PAGINA_URL = '/api/pagos';
const TIPO_ESTADO_PAGO = 'PAGO';
const ESTADO_ANULADO = 'ANULADO';

@Component({
  selector: 'app-pago-detail',
  imports: [SelectBuscable, TablaResponsiva,
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
    MatDatepickerModule,

  MatTooltipModule,  ],
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
  readonly reciboPago = inject(ReciboPago);

  readonly pago = signal<PagoResponse | null>(null);
  readonly comprobantes = signal<ComprobantePagoResponse[]>([]);
  readonly estados = signal<EstadoResponse[]>([]);
  readonly agregandoComprobante = signal(false);
  readonly guardando = signal(false);

  /** Foto o PDF elegido para el comprobante nuevo, ya comprimido si era una foto. */
  readonly archivoNuevo = signal<File | null>(null);
  readonly preparandoArchivo = signal(false);

  readonly puedeEditar: boolean;
  readonly puedeAgregar: boolean;
  readonly puedeEliminar: boolean;

  readonly tiposComprobante = TIPOS_COMPROBANTE;
  readonly archivosAceptados = ARCHIVOS_COMPROBANTE;
  readonly tamanoLegible = tamanoLegible;

  /**
   * Celular o tablet (se toca la pantalla en vez de usar mouse): solo ahi se ofrece "Tomar
   * foto". En una computadora no hay camara para fotografiar una boleta y el boton estorba.
   */
  readonly conCamara = window.matchMedia('(pointer: coarse)').matches;

  /** Un comprobante no puede ser de una fecha futura. */
  readonly hoy = new Date();

  readonly columnasComprobantes = ['numeroComprobante', 'tipoComprobante', 'fechaEmision', 'archivo', 'esValido', 'acciones'];

  readonly formularioComprobante = this.fb.nonNullable.group({
    numeroComprobante: ['', [Validators.required, Validators.maxLength(50)]],
    tipoComprobante: ['', Validators.required],
    fechaEmision: this.fb.control<Date | null>(null),
  });

  private idPago!: number;

  constructor() {
    this.puedeEditar = this.authService.tienePermiso(PAGINA_URL, 'modificacion');
    this.puedeAgregar = this.authService.tienePermiso(PAGINA_URL, 'alta');
    this.puedeEliminar = this.authService.tienePermiso(PAGINA_URL, 'baja');
  }

  /** Adjuntar o reemplazar el archivo: basta con poder dar de alta o modificar (igual que el backend). */
  get puedeAdjuntar(): boolean {
    return (this.puedeAgregar || this.puedeEditar) && !this.anulado;
  }

  /** Un pago anulado ya no se respalda con comprobantes. */
  get anulado(): boolean {
    return this.pago()?.estadoNombre === ESTADO_ANULADO;
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

  /** La foto de la camara o el archivo elegido; las fotos se reducen antes de guardarlas. */
  async elegirArchivo(evento: Event): Promise<void> {
    const input = evento.target as HTMLInputElement;
    const archivo = await this.prepararArchivo(input);
    if (archivo) {
      this.archivoNuevo.set(archivo);
    }
  }

  cancelarComprobante(): void {
    this.formularioComprobante.reset();
    this.archivoNuevo.set(null);
    this.agregandoComprobante.set(false);
  }

  agregarComprobante(): void {
    if (this.formularioComprobante.invalid) {
      this.formularioComprobante.markAllAsTouched();
      return;
    }
    const v = this.formularioComprobante.getRawValue();
    this.guardando.set(true);
    this.pagoService
      .agregarComprobante(
        this.idPago,
        {
          numeroComprobante: v.numeroComprobante.trim(),
          tipoComprobante: v.tipoComprobante,
          fechaEmision: v.fechaEmision ? this.aFechaIso(v.fechaEmision) : null,
          archivoUrl: null,
          esValido: true,
        },
        this.archivoNuevo(),
      )
      .subscribe({
        next: () => {
          this.snackBar.open('Comprobante agregado', 'Cerrar', { duration: 3000 });
          this.guardando.set(false);
          this.cancelarComprobante();
          this.cargar();
        },
        error: () => this.guardando.set(false),
      });
  }

  /** Adjunta el archivo a un comprobante que no lo tenia, o reemplaza el que tenia (foto borrosa, etc.). */
  async adjuntarArchivo(comprobante: ComprobantePagoResponse, evento: Event): Promise<void> {
    const input = evento.target as HTMLInputElement;
    const archivo = await this.prepararArchivo(input);
    if (!archivo) {
      return;
    }
    this.pagoService.guardarArchivoComprobante(this.idPago, comprobante.idComprobante, archivo).subscribe(() => {
      this.snackBar.open(comprobante.nombreArchivo ? 'Archivo reemplazado' : 'Archivo adjuntado', 'Cerrar', {
        duration: 3000,
      });
      this.cargar();
    });
  }

  /** Muestra el archivo dentro del sistema, sin salir de la pagina (en el celular no se podia regresar). */
  verArchivo(comprobante: ComprobantePagoResponse): void {
    this.pagoService.archivoComprobante(this.idPago, comprobante.idComprobante).subscribe((archivo) =>
      this.dialog.open(VisorArchivo, {
        data: {
          titulo: `${comprobante.tipoComprobante ?? 'Comprobante'} ${comprobante.numeroComprobante}`,
          nombre: comprobante.nombreArchivo ?? 'comprobante',
          archivo,
        },
        maxWidth: '95vw',
        autoFocus: false,
      }),
    );
  }

  /** Una boleta rechazada por el banco, un voucher reversado: queda registrado pero no cuenta. */
  cambiarValidez(comprobante: ComprobantePagoResponse): void {
    const marcarInvalido = comprobante.esValido;
    const ref = this.dialog.open(ConfirmDialog, {
      data: {
        titulo: marcarInvalido ? 'Marcar comprobante como inválido' : 'Volver a validar el comprobante',
        mensaje: marcarInvalido
          ? `¿Marcar "${comprobante.numeroComprobante}" como inválido? Por ejemplo, si el banco rechazó la boleta.`
          : `¿Marcar "${comprobante.numeroComprobante}" como válido otra vez?`,
      },
    });
    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.pagoService
        .actualizarComprobante(this.idPago, comprobante.idComprobante, {
          numeroComprobante: comprobante.numeroComprobante,
          tipoComprobante: comprobante.tipoComprobante ?? '',
          fechaEmision: comprobante.fechaEmision,
          archivoUrl: null,
          esValido: !comprobante.esValido,
        })
        .subscribe(() => {
          this.snackBar.open(marcarInvalido ? 'Comprobante marcado como inválido' : 'Comprobante válido', 'Cerrar', {
            duration: 3000,
          });
          this.cargar();
        });
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

  private aFechaIso(fecha: Date): string {
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }

  /** Comprime la foto y revisa el tamaño; deja el input listo para volver a elegir el mismo archivo. */
  private async prepararArchivo(input: HTMLInputElement): Promise<File | null> {
    const original = input.files?.[0];
    input.value = '';
    if (!original) {
      return null;
    }
    this.preparandoArchivo.set(true);
    try {
      const archivo = await comprimirImagen(original);
      if (archivo.size > TAMANO_MAXIMO_COMPROBANTE) {
        this.snackBar.open('El archivo pesa más de 5 MB', 'Cerrar', { duration: 4000 });
        return null;
      }
      return archivo;
    } finally {
      this.preparandoArchivo.set(false);
    }
  }
}
