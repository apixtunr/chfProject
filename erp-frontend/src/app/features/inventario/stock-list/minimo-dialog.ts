import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { InventarioResponse } from '../dto/inventario';

/** Dialogo para editar la cantidad minima (umbral de alerta) de un producto. */
@Component({
  selector: 'app-minimo-dialog',
  imports: [MatIconModule, FormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  template: `
    <h2 mat-dialog-title>Stock mínimo</h2>
    <mat-dialog-content>
      <p>{{ data.nombreProducto }}</p>
      <div class="pf-field campo">
        <label class="pf-label" for="minimo">
          <span class="pf-label-name">Cantidad mínima</span>
          <span class="pf-required-mark">*</span>
        </label>
        <div class="pf-input-wrap">
          <mat-icon class="pf-input-icon">tune</mat-icon>
          <input id="minimo" class="pf-input" type="number" min="0" step="1" [(ngModel)]="cantidadMinima" />
        </div>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-stroked-button class="pf-btn-cancel pf-btn-cancelar" [mat-dialog-close]="null">Cancelar</button>
      <button mat-flat-button class="pf-btn-save" [mat-dialog-close]="cantidadMinima" [disabled]="cantidadMinima === null || cantidadMinima < 0">
        Guardar
      </button>
    </mat-dialog-actions>
  `,
  styles: `.campo { width: 100%; }`,
})
export class MinimoDialog {
  cantidadMinima: number | null;

  constructor(
    public dialogRef: MatDialogRef<MinimoDialog>,
    @Inject(MAT_DIALOG_DATA) public data: InventarioResponse,
  ) {
    this.cantidadMinima = data.cantidadMinima;
  }
}
