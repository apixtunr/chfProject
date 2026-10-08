import { DecimalPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { ACUERDOS_ANTICIPO, AcuerdoAnticipo, CancelarEventoRequest } from '../dto/evento';
import { SelectBuscable } from '../../../shared/select-buscable';

export interface CancelarEventoDialogData {
  idEvento: number;
  /** Lo que el cliente ya pago del evento. */
  abonado: number;
}

/**
 * Cancelar un evento: el motivo es obligatorio y, si el cliente ya pago algo, en que quedo
 * ese dinero. No hay una politica fija; es lo que se negocio con el cliente.
 */
@Component({
  selector: 'app-cancelar-evento-dialog',
  imports: [SelectBuscable, ReactiveFormsModule, DecimalPipe, MatDialogModule, MatButtonModule, MatIconModule, MatSelectModule],
  templateUrl: './cancelar-evento-dialog.html',
  styleUrl: './cancelar-evento-dialog.scss',
})
export class CancelarEventoDialog {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<CancelarEventoDialog, CancelarEventoRequest>);
  readonly data = inject<CancelarEventoDialogData>(MAT_DIALOG_DATA);

  readonly acuerdos: AcuerdoAnticipo[] = ['RETENIDO', 'DEVUELTO', 'DEVUELTO_PARCIAL'];
  readonly etiquetaAcuerdo = ACUERDOS_ANTICIPO;
  readonly huboPagos = this.data.abonado > 0;

  readonly formulario = this.fb.group({
    motivo: ['', [Validators.required, Validators.maxLength(255)]],
    acuerdoAnticipo: this.fb.control<AcuerdoAnticipo | null>(null, this.huboPagos ? Validators.required : null),
    montoDevuelto: this.fb.control<number | null>(null),
  });

  constructor() {
    this.formulario.controls.acuerdoAnticipo.valueChanges.subscribe((acuerdo) => {
      const monto = this.formulario.controls.montoDevuelto;
      monto.setValidators(
        acuerdo === 'DEVUELTO_PARCIAL'
          ? [Validators.required, Validators.min(0.01), Validators.max(this.data.abonado - 0.01)]
          : null,
      );
      if (acuerdo !== 'DEVUELTO_PARCIAL') {
        monto.setValue(null);
      }
      monto.updateValueAndValidity();
    });
  }

  get devuelveParte(): boolean {
    return this.formulario.controls.acuerdoAnticipo.value === 'DEVUELTO_PARCIAL';
  }

  confirmar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const v = this.formulario.getRawValue();
    this.dialogRef.close({
      motivo: (v.motivo ?? '').trim(),
      acuerdoAnticipo: this.huboPagos ? v.acuerdoAnticipo : null,
      montoDevuelto: this.devuelveParte ? v.montoDevuelto : null,
    });
  }
}
