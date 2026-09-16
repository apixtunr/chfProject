import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { InventarioService } from '../inventario.service';
import { InventarioResponse } from '../dto/inventario';
import { MinimoDialog } from './minimo-dialog';
import { AgregarStockDialog } from './agregar-stock-dialog';

const PAGINA_URL = '/api/inventarios';
const PAGINA_URL_MOVIMIENTOS = '/api/movimientos-inventario';

@Component({
  selector: 'app-stock-list',
  imports: [
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatTooltipModule,
  ],
  templateUrl: './stock-list.html',
  styleUrl: './stock-list.scss',
})
export class StockList implements OnInit {
  private readonly inventarioService = inject(InventarioService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly inventarios = signal<InventarioResponse[]>([]);
  readonly alertas = signal<InventarioResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);

  readonly puedeEditarMinimo: boolean;
  readonly puedeAgregarStock: boolean;

  constructor() {
    this.puedeEditarMinimo = this.authService.tienePermiso(PAGINA_URL, 'modificacion');
    this.puedeAgregarStock = this.authService.tienePermiso(PAGINA_URL_MOVIMIENTOS, 'alta');
  }

  get columnas(): string[] {
    const base = ['nombreProducto', 'cantidadTotal', 'cantidadMinima', 'situacion'];
    return this.puedeEditarMinimo || this.puedeAgregarStock ? [...base, 'acciones'] : base;
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.inventarioService.listarInventarios(this.pageIndex(), this.pageSize()).subscribe((page) => {
      this.inventarios.set(page.content);
      this.totalElements.set(page.totalElements);
    });
    this.inventarioService.alertasBajoStock().subscribe((a) => this.alertas.set(a));
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.cargar();
  }

  bajoMinimo(inv: InventarioResponse): boolean {
    return inv.cantidadTotal < inv.cantidadMinima;
  }

  editarMinimo(inv: InventarioResponse): void {
    const ref = this.dialog.open(MinimoDialog, { data: inv, width: '360px' });

    ref.afterClosed().subscribe((cantidadMinima: number | null) => {
      if (cantidadMinima === null || cantidadMinima === undefined) {
        return;
      }
      this.inventarioService.actualizarMinimo(inv.idProducto, cantidadMinima).subscribe(() => {
        this.snackBar.open('Stock minimo actualizado', 'Cerrar', { duration: 3000 });
        this.cargar();
      });
    });
  }

  agregarStock(inv: InventarioResponse): void {
    const ref = this.dialog.open(AgregarStockDialog, { data: inv, width: '360px' });

    ref.afterClosed().subscribe((resultado) => {
      if (!resultado) {
        return;
      }
      this.inventarioService
        .registrarMovimiento({
          idProducto: inv.idProducto,
          tipoMovimiento: 'ENTRADA',
          cantidad: resultado.cantidad,
          descripcion: resultado.descripcion,
          idEvento: null,
        })
        .subscribe(() => {
          this.snackBar.open('Stock agregado', 'Cerrar', { duration: 3000 });
          this.cargar();
        });
    });
  }
}
