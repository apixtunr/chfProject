import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { UsuarioResponse } from '../dto/admin';

@Component({
  selector: 'app-password-dialog',
  imports: [MatIconModule, FormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  template: `
    <h2 mat-dialog-title>Cambiar contraseña</h2>
    <mat-dialog-content>
      <p>Usuario: {{ data.username }}</p>
      <div class="pf-field campo">
        <label class="pf-label" for="password">
          <span class="pf-label-name">Nueva contraseña</span>
          <span class="pf-required-mark">*</span>
          <span class="pf-label-hint">(mínimo 8 caracteres)</span>
        </label>
        <div class="pf-input-wrap">
          <mat-icon class="pf-input-icon">lock</mat-icon>
          <input id="password" class="pf-input" type="password" [(ngModel)]="password" autocomplete="new-password" />
        </div>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-stroked-button class="pf-btn-cancel pf-btn-cancelar" [mat-dialog-close]="null">Cancelar</button>
      <button mat-flat-button class="pf-btn-save" [mat-dialog-close]="password" [disabled]="password.length < 8">
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
