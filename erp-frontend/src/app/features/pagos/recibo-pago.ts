import { Injectable, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { VisorArchivo } from '../../shared/visor-archivo/visor-archivo';
import { PagoService } from './pago.service';

/** "REC-000045": el numero del recibo es el del pago (igual que en el backend). */
export function numeroRecibo(idPago: number): string {
  return `REC-${String(idPago).padStart(6, '0')}`;
}

/**
 * Muestra el recibo de un pago dentro del sistema, con boton para descargarlo (y en el
 * celular, para compartirlo por WhatsApp). Lo usan el detalle del pago y la lista de
 * abonos del evento.
 */
@Injectable({ providedIn: 'root' })
export class ReciboPago {
  private readonly pagoService = inject(PagoService);
  private readonly dialog = inject(MatDialog);

  ver(idPago: number): void {
    this.pagoService.recibo(idPago).subscribe((archivo) =>
      this.dialog.open(VisorArchivo, {
        data: { titulo: `Recibo ${numeroRecibo(idPago)}`, nombre: `${numeroRecibo(idPago)}.pdf`, archivo },
        maxWidth: '95vw',
        autoFocus: false,
      }),
    );
  }
}
