import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { UsuarioResponse } from '../dto/admin';

@Component({
  selector: 'app-password-dialog',
  imports: [FormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  template: `
    <h2 mat-dialog-title>Cambiar contrasena</h2>
    <mat-dialog-content>
      <p>Usuario: {{ data.username }}</p>
      <mat-form-field appearance="outline" class="campo">
        <mat-label>Nueva contrasena (min. 8 caracteres)</mat-label>
        <input matInput type="password" [(ngModel)]="password" />
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="null">Cancelar</button>
      <button mat-flat-button color="primary" [mat-dialog-close]="password" [disabled]="password.length < 8">
        Guardar
      </button>
    </mat-dialog-actions>
  `,
  styles: `.campo { width: 100%; }`,
})
export class PasswordDialog {
  password = '';

  constructor(
    public dialogRef: MatDialogRef<PasswordDialog>,
    @Inject(MAT_DIALOG_DATA) public data: UsuarioResponse,
  ) {}
}
