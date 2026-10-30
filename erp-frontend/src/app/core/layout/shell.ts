import { BreakpointObserver } from '@angular/cdk/layout';
import {
  Component,
  ElementRef,
  afterRenderEffect,
  computed,
  effect,
  inject,
  signal,
  viewChild,
  viewChildren,
} from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenav, MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map, startWith, switchMap } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { MenuModulo, MenuService } from './menu.service';

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

  readonly esCompacto = toSignal(
    this.breakpoints.observe('(max-width: 1023.98px)').pipe(map((estado) => estado.matches)),
    { initialValue: false },
  );

  private readonly sidenav = viewChild.required<MatSidenav>('sidenav');

  private readonly menuService = inject(MenuService);

  readonly gruposMenu = toSignal(
    toObservable(this.authService.permisos).pipe(switchMap(() => this.menuService.obtener())),
    { initialValue: [] as MenuModulo[] },
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
    return this.gruposMenu().find((grupo) => grupo.pantallas.some((p) => url.startsWith(p.ruta)))?.modulo ?? null;
  });

  readonly moduloSeleccionado = signal<string | null>(null);

  readonly grupoSeleccionado = computed(
    () => this.gruposMenu().find((grupo) => grupo.modulo === this.moduloSeleccionado()) ?? null,
  );

  private readonly botonesModulo = viewChildren('botonModulo', { read: ElementRef });
  private readonly opcionesPanel = viewChild('opcionesPanel', { read: ElementRef });

  readonly desplazamientoPanel = signal(0);

  constructor() {
    this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd))
      .subscribe(() => {
        if (this.esCompacto()) {
          this.sidenav().close();
        }
      });

    effect(() => this.moduloSeleccionado.set(this.moduloActivo()));

    afterRenderEffect(() => {
      const indice = this.gruposMenu().findIndex((grupo) => grupo.modulo === this.moduloSeleccionado());
      const boton = this.botonesModulo()[indice]?.nativeElement as HTMLElement | undefined;
      const opciones = this.opcionesPanel()?.nativeElement as HTMLElement | undefined;
      if (!boton || !opciones) {
        return;
      }
      const panel = opciones.parentElement!;
      const pie = panel.querySelector<HTMLElement>('.empresa-sidebar');
      const items = opciones.querySelectorAll<HTMLElement>('.mat-mdc-list-item');
      const inicioOpciones = opciones.getBoundingClientRect().top - parseFloat(opciones.style.marginTop || '0');
      const primero = items[0].getBoundingClientRect().top - opciones.getBoundingClientRect().top;
      const altoItems = items[items.length - 1].getBoundingClientRect().bottom - items[0].getBoundingClientRect().top;
      const centroBoton = boton.getBoundingClientRect().top + boton.offsetHeight / 2;
      const deseado = centroBoton - altoItems / 2 - primero - inicioOpciones;
      const espacio = panel.getBoundingClientRect().bottom - inicioOpciones
        - opciones.offsetHeight - (pie ? pie.offsetHeight + 32 : 0);
      this.desplazamientoPanel.set(Math.max(0, Math.min(deseado, espacio)));
    });
  }

  elegirModulo(grupo: MenuModulo): void {
    if (grupo.pantallas.length === 1) {
      this.router.navigateByUrl(grupo.pantallas[0].ruta);
      return;
    }
    this.moduloSeleccionado.set(grupo.modulo);
  }

  ocultarPanel(): void {
    this.moduloSeleccionado.set(null);
  }

  irAInicio(): void {
    this.moduloSeleccionado.set(null);
  }

  cerrarSesion(): void {
    this.authService.logout();
  }
}
