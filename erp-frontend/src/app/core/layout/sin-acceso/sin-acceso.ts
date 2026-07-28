import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-sin-acceso',
  imports: [MatButtonModule, MatIconModule],
  templateUrl: './sin-acceso.html',
  styleUrl: './sin-acceso.scss',
})
export class SinAcceso {
  private readonly authService = inject(AuthService);

  cerrarSesion(): void {
    this.authService.logout();
  }
}
