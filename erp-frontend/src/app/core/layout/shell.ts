import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, computed, effect, inject, signal, viewChild } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenav, MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { NAV_GROUPS, NavGroup } from './nav-items';

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
  ],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly breakpoints = inject(BreakpointObserver);

  /** Debajo de 1024px (celulares y tabletas) el menu deja de estar fijo y se abre encima. */
  readonly esCompacto = toSignal(
    this.breakpoints.observe('(max-width: 1023.98px)').pipe(map((estado) => estado.matches)),
    { initialValue: false },
  );

  private readonly sidenav = viewChild.required<MatSidenav>('sidenav');

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

  /** El modulo de la pantalla abierta (ninguno en Inicio). */
  private readonly moduloActivo = computed(() => {
    const url = this.urlActual();
    return NAV_GROUPS.find((grupo) => grupo.items.some((item) => url.startsWith(item.route)))?.modulo ?? null;
  });

  /**
   * El modulo cuyas pantallas se muestran en el panel. Sigue a la pantalla abierta, pero se
   * puede elegir otro para ver sus pantallas sin salir de la actual.
   */
  readonly moduloSeleccionado = signal<string | null>(null);

  readonly grupoSeleccionado = computed(
    () => this.gruposMenu().find((grupo) => grupo.modulo === this.moduloSeleccionado()) ?? null,
  );

  constructor() {
    this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd))
      .subscribe(() => {
        if (this.esCompacto()) {
          this.sidenav().close();
        }
      });

    effect(() => this.moduloSeleccionado.set(this.moduloActivo()));
  }

  /** Un modulo de una sola pantalla la abre; los demas muestran sus pantallas en el panel. */
  elegirModulo(grupo: NavGroup): void {
    if (grupo.items.length === 1) {
      this.router.navigateByUrl(grupo.items[0].route);
      return;
    }
    this.moduloSeleccionado.set(grupo.modulo);
  }

  cerrarSesion(): void {
    this.authService.logout();
  }
}
