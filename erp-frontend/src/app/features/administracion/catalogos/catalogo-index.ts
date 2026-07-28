import { Component, inject } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { CATALOGOS, CatalogoConfig } from './catalogos-config';

/** Indice de catalogos: muestra solo los que el rol puede ver. */
@Component({
  selector: 'app-catalogo-index',
  imports: [RouterLink, MatCardModule, MatIconModule],
  template: `
    <h1>Catalogos del sistema</h1>
    <div class="cuadricula">
      @for (catalogo of visibles; track catalogo.id) {
        <mat-card class="tarjeta" [routerLink]="['/admin/catalogos', catalogo.id]">
          <mat-icon>list_alt</mat-icon>
          <span>{{ catalogo.titulo }}</span>
        </mat-card>
      } @empty {
        <p>No tienes acceso a ningun catalogo.</p>
      }
    </div>
  `,
  styles: `
    .cuadricula {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      gap: 12px;
      margin-top: 16px;
    }
    .tarjeta {
      display: flex;
      flex-direction: row;
      align-items: center;
      gap: 12px;
      padding: 16px;
      cursor: pointer;
      transition: box-shadow 0.15s;
      &:hover { box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); }
    }
  `,
})
export class CatalogoIndex {
  private readonly authService = inject(AuthService);

  readonly visibles: CatalogoConfig[] = CATALOGOS.filter((c) => this.authService.puedeVer(c.paginaUrl));
}
