import { Component, computed, inject } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-home',
  imports: [],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {
  private readonly authService = inject(AuthService);

  readonly usuario = computed(() => this.authService.usuarioActual());

  readonly saludo = computed(() => {
    const hora = new Date().getHours();
    if (hora < 12) return 'Buenos dias';
    if (hora < 19) return 'Buenas tardes';
    return 'Buenas noches';
  });
}
