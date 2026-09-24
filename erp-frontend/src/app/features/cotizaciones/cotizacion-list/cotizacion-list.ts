import { CommonModule, LowerCasePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { CotizacionService } from '../cotizacion.service';
import { CotizacionResponse } from '../dto/cotizacion';

const PAGINA_URL = '/api/cotizaciones';

@Component({
  selector: 'app-cotizacion-list',
  imports: [
    CommonModule,
    LowerCasePipe,
    FormsModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
  ],
  templateUrl: './cotizacion-list.html',
  styleUrl: './cotizacion-list.scss',
})
export class CotizacionList implements OnInit {
  private readonly cotizacionService = inject(CotizacionService);
  private readonly authService = inject(AuthService);

  readonly cotizaciones = signal<CotizacionResponse[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);

  readonly columnas = ['cliente', 'tipoEvento', 'fechaCotizacion', 'estado', 'monto', 'acciones'];

  get puedeCrear(): boolean {
    return this.authService.tienePermiso(PAGINA_URL, 'alta');
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cotizacionService.listar(null, this.pageIndex(), this.pageSize()).subscribe((page) => {
      this.cotizaciones.set(page.content);
      this.totalElements.set(page.totalElements);
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.cargar();
  }



}
