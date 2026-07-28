import { Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { NAV_GROUPS } from './nav-items';

@Component({
  selector: 'app-shell',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatExpansionModule,
  ],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  /** Modulos con sus pantallas visibles segun permisos; modulos vacios se ocultan. */
  readonly gruposMenu = computed(() =>
    NAV_GROUPS.map((grupo) => ({
      ...grupo,
      items: grupo.items.filter((item) => this.authService.puedeVer(item.paginaUrl)),
    })).filter((grupo) => grupo.items.length > 0),
  );

  readonly usuario = computed(() => this.authService.usuarioActual());

  private readonly urlActual = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map((e) => e.urlAfterRedirects),
      startWith(this.router.url),
    ),
    { initialValue: this.router.url },
  );

  private readonly moduloActivo = computed(() => {
    const url = this.urlActual();
    return NAV_GROUPS.find((grupo) => grupo.items.some((item) => url.startsWith(item.route)))?.modulo ?? null;
  });

  /** Paneles abiertos: arranca vacio (todo colapsado) y se va abriendo con la navegacion o a mano. */
  private readonly modulosExpandidos = signal<ReadonlySet<string>>(new Set());

  constructor() {
    effect(() => {
      const activo = this.moduloActivo();
      if (activo && !this.modulosExpandidos().has(activo)) {
        this.modulosExpandidos.update((actual) => new Set(actual).add(activo));
      }
    });
  }

  estaExpandido(modulo: string): boolean {
    return this.modulosExpandidos().has(modulo);
  }

  alternarModulo(modulo: string, expandido: boolean): void {
    this.modulosExpandidos.update((actual) => {
      const nuevo = new Set(actual);
      if (expandido) {
        nuevo.add(modulo);
      } else {
        nuevo.delete(modulo);
      }
      return nuevo;
    });
  }

  cerrarSesion(): void {
    this.authService.logout();
  }
}
