import { Injectable, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { VisorArchivo } from '../../shared/visor-archivo/visor-archivo';
import { PagoService } from './pago.service';

export function numeroRecibo(idPago: number): string {
  return String(idPago).padStart(6, '0');
}

@Injectable({ providedIn: 'root' })
export class ReciboPago {
  private readonly pagoService = inject(PagoService);
  private readonly dialog = inject(MatDialog);

  ver(idPago: number): void {
    this.pagoService.recibo(idPago).subscribe((archivo) =>
      this.dialog.open(VisorArchivo, {
        data: { titulo: `Recibo No. ${numeroRecibo(idPago)}`, nombre: `Recibo-${numeroRecibo(idPago)}.pdf`, archivo },
        maxWidth: '95vw',
        autoFocus: false,
      }),
    );
  }
}
