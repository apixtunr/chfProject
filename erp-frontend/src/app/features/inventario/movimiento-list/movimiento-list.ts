import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { InventarioService } from '../inventario.service';
import { MovimientoInventarioResponse, ProductoResponse } from '../dto/inventario';

const PAGINA_URL = '/api/movimientos-inventario';

@Component({
  selector: 'app-movimiento-list',
  imports: [
    RouterLink,
    DatePipe,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTooltipModule,
  ],
  templateUrl: './movimiento-list.html',
  styleUrl: './movimiento-list.scss',
})
export class MovimientoList implements OnInit {
  private readonly inventarioService = inject(InventarioService);
  private readonly authService = inject(AuthService);

  readonly movimientos = signal<MovimientoInventarioResponse[]>([]);
  readonly productos = signal<ProductoResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);
  readonly tipoFiltro = signal<'producto' | 'evento'>('producto');
  readonly idProductoFiltro = signal<number | null>(null);
  readonly idEventoFiltro = signal<number | null>(null);

  /** Valor del input, separado de idEventoFiltro para no disparar la busqueda en cada tecla. */
  idEventoInput: number | null = null;

  readonly puedeRegistrar: boolean;

  readonly columnas = ['fechaMovimiento', 'producto', 'tipo', 'cantidad', 'stockResultante', 'usuario', 'descripcion'];

  constructor() {
    this.puedeRegistrar = this.authService.tienePermiso(PAGINA_URL, 'alta');
  }

  ngOnInit(): void {
    // Catalogo para el filtro (mismo tope que usa el resto de la app para dropdowns)
    this.inventarioService.listarProductos('', 0, 200).subscribe((p) => this.productos.set(p.content));
    this.cargar();
  }

  cargar(): void {
    this.inventarioService
      .listarMovimientos(this.idProductoFiltro(), this.idEventoFiltro(), this.pageIndex(), this.pageSize())
      .subscribe((page) => {
        this.movimientos.set(page.content);
        this.totalElements.set(page.totalElements);
      });
  }

  /** Al cambiar de tipo se limpia el filtro que ya no aplica (nunca conviven los dos a la vez). */
  cambiarTipoFiltro(tipo: 'producto' | 'evento'): void {
    this.tipoFiltro.set(tipo);
    if (tipo === 'producto') {
      this.idEventoInput = null;
      this.idEventoFiltro.set(null);
    } else {
      this.idProductoFiltro.set(null);
    }
    this.pageIndex.set(0);
    this.cargar();
  }

  filtrarPorProducto(idProducto: number | null): void {
    this.idProductoFiltro.set(idProducto);
    this.pageIndex.set(0);
    this.cargar();
  }

  filtrarPorEvento(): void {
    this.idEventoFiltro.set(this.idEventoInput);
    this.pageIndex.set(0);
    this.cargar();
  }

  limpiarFiltroEvento(): void {
    this.idEventoInput = null;
    this.idEventoFiltro.set(null);
    this.pageIndex.set(0);
    this.cargar();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.cargar();
  }
}
