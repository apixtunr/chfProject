import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
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
  imports: [MatIconModule, FormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  template: `
    <h2 mat-dialog-title>Agregar stock</h2>
    <mat-dialog-content>
      <p>{{ data.nombreProducto }} <span class="disponible">(disponible: {{ data.cantidadTotal }})</span></p>
      <div class="pf-field campo">
        <label class="pf-label" for="cantidad">
          <span class="pf-label-name">Cantidad a ingresar</span>
          <span class="pf-required-mark">*</span>
        </label>
        <div class="pf-input-wrap">
          <mat-icon class="pf-input-icon">add_box</mat-icon>
          <input id="cantidad" class="pf-input" type="number" min="0.01" step="0.01" [(ngModel)]="cantidad" />
        </div>
      </div>
      <div class="pf-field campo">
        <label class="pf-label" for="descripcion">
          <span class="pf-label-name">Descripción</span>
          
          <span class="pf-label-hint">(opcional)</span>
        </label>
        <div class="pf-input-wrap">
          <mat-icon class="pf-input-icon">notes</mat-icon>
          <input id="descripcion" class="pf-input" [(ngModel)]="descripcion" />
        </div>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-stroked-button class="pf-btn-cancel pf-btn-cancelar" [mat-dialog-close]="null">Cancelar</button>
      <button
        mat-flat-button
        class="pf-btn-save"
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
