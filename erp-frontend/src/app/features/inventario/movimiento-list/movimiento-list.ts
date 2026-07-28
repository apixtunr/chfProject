import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
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
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
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
  readonly idProductoFiltro = signal<number | null>(null);

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
      .listarMovimientos(this.idProductoFiltro(), this.pageIndex(), this.pageSize())
      .subscribe((page) => {
        this.movimientos.set(page.content);
        this.totalElements.set(page.totalElements);
      });
  }

  filtrarPorProducto(idProducto: number | null): void {
    this.idProductoFiltro.set(idProducto);
    this.pageIndex.set(0);
    this.cargar();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.cargar();
  }
}
