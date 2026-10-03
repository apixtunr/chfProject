import { AfterViewInit, Directive, ElementRef, OnDestroy, inject } from '@angular/core';

/**
 * En celular las tablas se dibujan como tarjetas (una por fila) y el encabezado se
 * oculta; cada celda muestra entonces el nombre de su columna a la izquierda. Ese
 * nombre se toma del encabezado real de la tabla y se copia en data-label, que es lo
 * que lee el CSS de styles.scss. Asi ninguna pantalla tiene que repetir sus titulos.
 *
 * Las filas llegan y cambian despues (paginacion, filtros, busqueda), por eso se
 * vuelve a etiquetar cada vez que el cuerpo de la tabla cambia.
 */
@Directive({
  selector: 'table.tabla',
  host: { class: 'tabla-responsiva' },
})
export class TablaResponsiva implements AfterViewInit, OnDestroy {
  private readonly tabla = inject<ElementRef<HTMLTableElement>>(ElementRef).nativeElement;
  private observador?: MutationObserver;
  private pendiente = false;

  ngAfterViewInit(): void {
    this.etiquetar();
    this.observador = new MutationObserver(() => this.programar());
    this.observador.observe(this.tabla, { childList: true, subtree: true, characterData: true });
  }

  ngOnDestroy(): void {
    this.observador?.disconnect();
  }

  private programar(): void {
    if (this.pendiente) return;
    this.pendiente = true;
    requestAnimationFrame(() => {
      this.pendiente = false;
      this.etiquetar();
    });
  }

  private etiquetar(): void {
    const titulos = new Map<string, string>();
    this.tabla.querySelectorAll<HTMLElement>('th.mat-mdc-header-cell').forEach((th) => {
      const columna = this.columnaDe(th);
      if (columna) titulos.set(columna, th.textContent?.trim() ?? '');
    });

    this.tabla.querySelectorAll<HTMLElement>('td.mat-mdc-cell').forEach((td) => {
      const titulo = titulos.get(this.columnaDe(td) ?? '') ?? '';
      if (td.getAttribute('data-label') !== titulo) td.setAttribute('data-label', titulo);
    });
  }

  private columnaDe(celda: HTMLElement): string | undefined {
    return Array.from(celda.classList).find((c) => c.startsWith('mat-column-'));
  }
}
