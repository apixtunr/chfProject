import { AfterContentInit, DestroyRef, Directive, Renderer2, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatOption } from '@angular/material/core';
import { MatSelect } from '@angular/material/select';

/** Con mas opciones que esto la lista ya no se recorre de un vistazo y conviene buscar. */
const OPCIONES_PARA_BUSCAR = 8;

/** Minusculas y sin tildes: "platano" encuentra "Plátano maduro". */
function normalizar(texto: string): string {
  return texto.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().trim();
}

/**
 * Agrega un buscador arriba de la lista de cualquier mat-select con mas de 8 opciones.
 * Las listas cortas (estado, metodo de pago) se quedan como estan: ahi un buscador
 * estorba mas de lo que ayuda.
 *
 * Se escribe una vez y aplica a todos los mat-select de las pantallas que lo importan,
 * sin tocar sus plantillas. Las opciones que no coinciden solo se ocultan: el valor y
 * el formulario no cambian. Enter elige la primera coincidencia y Esc cierra la lista.
 */
@Directive({ selector: 'mat-select' })
export class SelectBuscable implements AfterContentInit {
  private readonly select = inject(MatSelect);
  private readonly renderer = inject(Renderer2);

  private readonly destroyRef = inject(DestroyRef);

  constructor() {
    this.select.openedChange
      .pipe(takeUntilDestroyed(this.destroyRef))
      // El aviso de "abierto" llega antes de que Material dibuje el panel: se espera un
      // ciclo para encontrarlo ya en pantalla.
      .subscribe((abierto) => (abierto ? setTimeout(() => this.alAbrir()) : this.mostrarTodas()));
  }

  ngAfterContentInit(): void {
    // Con una base lejana las opciones pueden llegar con la lista ya abierta: al abrirla
    // no habia suficientes para poner el buscador, asi que se revisa de nuevo al llegar.
    this.select.options.changes.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      if (this.select.panelOpen) {
        setTimeout(() => this.alAbrir());
      }
    });
  }

  private alAbrir(): void {
    const panel: HTMLElement | undefined = this.select.panel?.nativeElement;
    if (!panel || this.select.options.length <= OPCIONES_PARA_BUSCAR || panel.querySelector('.select-buscador')) {
      return;
    }

    const caja = this.renderer.createElement('div') as HTMLElement;
    this.renderer.addClass(caja, 'select-buscador');
    const input = this.renderer.createElement('input') as HTMLInputElement;
    input.type = 'text';
    input.placeholder = 'Buscar...';
    input.setAttribute('aria-label', 'Buscar en la lista');
    input.autocomplete = 'off';
    this.renderer.appendChild(caja, input);
    this.renderer.insertBefore(panel, caja, panel.firstChild);

    this.renderer.listen(input, 'input', () => this.filtrar(input.value));
    // Las teclas se quedan en el buscador: si llegaran al mat-select, la barra espaciadora
    // elegiria una opcion y las letras moverian la seleccion en vez de escribir.
    this.renderer.listen(input, 'keydown', (evento: KeyboardEvent) => {
      evento.stopPropagation();
      if (evento.key === 'Escape') {
        this.select.close();
        this.select.focus();
      } else if (evento.key === 'Enter') {
        evento.preventDefault();
        const primera = this.visibles()[0];
        if (primera) {
          primera._selectViaInteraction();
          if (!this.select.multiple) {
            this.select.focus();
          }
        }
      }
    });

    input.focus();
  }

  private filtrar(texto: string): void {
    const buscado = normalizar(texto);
    this.select.options.forEach((opcion) => {
      const coincide = !buscado || normalizar(opcion.viewValue).includes(buscado);
      this.renderer.setStyle(opcion._getHostElement(), 'display', coincide ? '' : 'none');
    });
  }

  private visibles(): MatOption[] {
    return this.select.options.filter(
      (opcion) => !opcion.disabled && opcion._getHostElement().style.display !== 'none',
    );
  }

  /** Al cerrar, todas las opciones vuelven a verse para la proxima vez. */
  private mostrarTodas(): void {
    this.select.options.forEach((opcion) =>
      this.renderer.removeStyle(opcion._getHostElement(), 'display'),
    );
  }
}
