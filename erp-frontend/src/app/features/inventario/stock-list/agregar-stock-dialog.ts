import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { InventarioResponse } from '../dto/inventario';

export interface ResultadoAgregarStock {
  cantidad: number;
  descripcion: string | null;
}

/** Atajo para cargar stock sin ir hasta Movimientos: por debajo registra una ENTRADA igual. */
@Component({
  selector: 'app-agregar-stock-dialog',
  imports: [FormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  template: `
    <h2 mat-dialog-title>Agregar stock</h2>
    <mat-dialog-content>
      <p>{{ data.nombreProducto }} <span class="disponible">(disponible: {{ data.cantidadTotal }})</span></p>
      <mat-form-field appearance="outline" class="campo">
        <mat-label>Cantidad a ingresar</mat-label>
        <input matInput type="number" min="0.01" step="0.01" [(ngModel)]="cantidad" />
      </mat-form-field>
      <mat-form-field appearance="outline" class="campo">
        <mat-label>Descripción (opcional)</mat-label>
        <input matInput [(ngModel)]="descripcion"/>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="null">Cancelar</button>
      <button
        mat-flat-button
        color="primary"
        [mat-dialog-close]="{ cantidad: cantidad!, descripcion: descripcion }"
        [disabled]="cantidad === null || cantidad <= 0"
      >
        Registrar entrada
      </button>
    </mat-dialog-actions>
  `,
  styles: `
    .campo { width: 100%; }
    .disponible { color: var(--mat-sys-on-surface-variant); font-size: 0.85rem; }
  `,
})
export class AgregarStockDialog {
  cantidad: number | null = null;
  descripcion: string | null = null;

  constructor(
    public dialogRef: MatDialogRef<AgregarStockDialog, ResultadoAgregarStock | null>,
    @Inject(MAT_DIALOG_DATA) public data: InventarioResponse,
  ) {}
}
