import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { InventarioResponse } from '../dto/inventario';

/** Dialogo para editar la cantidad minima (umbral de alerta) de un producto. */
@Component({
  selector: 'app-minimo-dialog',
  imports: [FormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  template: `
    <h2 mat-dialog-title>Stock minimo</h2>
    <mat-dialog-content>
      <p>{{ data.nombreProducto }}</p>
      <mat-form-field appearance="outline" class="campo">
        <mat-label>Cantidad minima</mat-label>
        <input matInput type="number" min="0" step="1" [(ngModel)]="cantidadMinima" />
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="null">Cancelar</button>
      <button mat-flat-button color="primary" [mat-dialog-close]="cantidadMinima" [disabled]="cantidadMinima === null || cantidadMinima < 0">
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
