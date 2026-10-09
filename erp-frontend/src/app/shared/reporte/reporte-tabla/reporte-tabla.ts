import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, EnvironmentInjector, OnInit, inject, runInInjectionContext, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute } from '@angular/router';
import { debounceTime, distinctUntilChanged, of, switchMap } from 'rxjs';
import { API_URL } from '../../../core/api-config';
import { AuthService } from '../../../core/auth/auth.service';
import { ClienteService } from '../../../features/clientes/cliente.service';
import { ClienteResponse } from '../../../features/clientes/dto/cliente';
import { VerPdf, aFechaIso, descargarCsv, mesActual, parametros } from '../exportar';
import {
  AGRUPACIONES_FECHA,
  ColumnaReporte,
  ConfigReporte,
  FilaReporte,
  FiltroReporte,
  OpcionReporte,
  ReporteTabla,
} from './reporte-config';

/**
 * Pantalla de un reporte por modulo: filtros, agrupacion por periodo, tabla con subtotales
 * por grupo y total general, e Imprimir PDF / Exportar CSV segun los permisos del rol.
 * Todos los reportes la usan; lo que cambia viene en la configuracion de su ruta.
 */
@Component({
  selector: 'app-reporte-tabla',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatDatepickerModule,
    MatIconModule,
    MatSelectModule,
  ],
  providers: [DatePipe, DecimalPipe],
  templateUrl: './reporte-tabla.html',
})
export class ReporteTablaPage implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly clienteService = inject(ClienteService);
  private readonly injector = inject(EnvironmentInjector);
  private readonly verPdf = inject(VerPdf);
  private readonly datePipe = inject(DatePipe);
  private readonly decimalPipe = inject(DecimalPipe);

  readonly config: ConfigReporte = inject(ActivatedRoute).snapshot.data['reporte'];

  readonly reporte = signal<ReporteTabla | null>(null);
  readonly opciones = signal<Record<string, OpcionReporte[]>>({});
  readonly agrupaciones: OpcionReporte[] = [...AGRUPACIONES_FECHA, ...(this.config.agrupaciones ?? [])];

  readonly puedeImprimir = this.authService.tienePermiso(this.config.paginaUrl, 'imprimir');
  readonly puedeExportar = this.authService.tienePermiso(this.config.paginaUrl, 'exportar');

  private readonly periodoInicial = this.config.fechas.porDefecto === 'mes' ? mesActual() : null;

  readonly fechaDesde = new FormControl<Date | null>(this.periodoInicial?.desde ?? null);
  readonly fechaHasta = new FormControl<Date | null>(this.periodoInicial?.hasta ?? null);
  readonly fechaSegun = new FormControl<string | number | null>(this.config.fechaSegun?.[0]?.valor ?? null);
  readonly agrupar = new FormControl<string | number | null>(null);
  readonly filtros = new FormGroup<Record<string, FormControl<string | number | null>>>(
    Object.fromEntries(this.config.filtros.map((f) => [f.clave, new FormControl<string | number | null>(null)])),
  );

  /** Autocomplete de cliente, para los reportes que filtran por cliente. */
  readonly busquedaCliente = new FormControl<ClienteResponse | string>('', { nonNullable: true });
  readonly clientesFiltrados = signal<ClienteResponse[]>([]);

  ngOnInit(): void {
    for (const filtro of this.config.filtros) {
      if (filtro.tipo === 'lista' && filtro.opciones) {
        runInInjectionContext(this.injector, filtro.opciones).subscribe((lista) =>
          this.opciones.update((o) => ({ ...o, [filtro.clave]: lista })),
        );
      }
    }

    const filtroCliente = this.config.filtros.find((f) => f.tipo === 'cliente');
    if (filtroCliente) {
      this.busquedaCliente.valueChanges.subscribe((texto) => {
        if (typeof texto === 'string' && !texto.trim()) {
          this.filtros.controls[filtroCliente.clave].setValue(null);
          this.clientesFiltrados.set([]);
        }
      });
      this.busquedaCliente.valueChanges
        .pipe(
          debounceTime(300),
          distinctUntilChanged(),
          switchMap((texto) => {
            const valor = typeof texto === 'string' ? texto.trim() : '';
            return valor ? this.clienteService.listar(valor, 0, 10) : of(null);
          }),
        )
        .subscribe((page) => this.clientesFiltrados.set(page?.content ?? []));
    }

    this.cargar();
  }

  mostrarCliente(cliente: ClienteResponse | string | null): string {
    return cliente && typeof cliente !== 'string' ? cliente.nombre : '';
  }

  seleccionarCliente(filtro: FiltroReporte, event: MatAutocompleteSelectedEvent): void {
    const valor = event.option.value as ClienteResponse | null;
    this.filtros.controls[filtro.clave].setValue(valor ? valor.idCliente : null);
  }

  private valoresConsulta(): Record<string, string | number | null> {
    return {
      fechaDesde: aFechaIso(this.fechaDesde.value),
      fechaHasta: aFechaIso(this.fechaHasta.value),
      fechaSegun: this.fechaSegun.value,
      agrupar: this.agrupar.value,
      ...this.filtros.getRawValue(),
    };
  }

  cargar(): void {
    this.http
      .get<ReporteTabla>(`${API_URL}/${this.config.url}`, { params: parametros(this.valoresConsulta()) })
      .subscribe((r) => this.reporte.set(r));
  }

  limpiar(): void {
    this.fechaDesde.setValue(this.periodoInicial?.desde ?? null);
    this.fechaHasta.setValue(this.periodoInicial?.hasta ?? null);
    this.fechaSegun.setValue(this.config.fechaSegun?.[0]?.valor ?? null);
    this.agrupar.setValue(null);
    this.filtros.reset();
    this.busquedaCliente.setValue('');
    this.clientesFiltrados.set([]);
    this.cargar();
  }

  /** Los filtros aplicados, en texto, para el subtitulo del PDF. */
  private textoFiltros(): string {
    const partes: string[] = [];
    if (this.config.fechaSegun && this.fechaSegun.value !== null) {
      const segun = this.config.fechaSegun.find((o) => o.valor === this.fechaSegun.value);
      if (segun) {
        partes.push(`Según ${segun.etiqueta.toLowerCase()}`);
      }
    }
    for (const filtro of this.config.filtros) {
      const valor = this.filtros.controls[filtro.clave].value;
      if (valor === null) {
        continue;
      }
      if (filtro.tipo === 'cliente') {
        const cliente = this.busquedaCliente.value;
        partes.push(`${filtro.etiqueta}: ${typeof cliente === 'string' ? cliente : cliente.nombre}`);
      } else {
        const opcion = this.opciones()[filtro.clave]?.find((o) => o.valor === valor);
        partes.push(`${filtro.etiqueta}: ${opcion?.etiqueta ?? valor}`);
      }
    }
    const agrupacion = this.agrupaciones.find((a) => a.valor === this.agrupar.value);
    if (agrupacion) {
      partes.push(`Agrupado ${agrupacion.etiqueta.toLowerCase()}`);
    }
    return partes.join('  ·  ');
  }

  imprimir(): void {
    const pdf = this.http.get(`${API_URL}/${this.config.url}/pdf`, {
      params: parametros({ ...this.valoresConsulta(), filtros: this.textoFiltros() }),
      responseType: 'blob',
    });
    this.verPdf.abrir(pdf, this.config.titulo, this.config.archivo);
  }

  /** Una fila por registro; si esta agrupado, la primera columna dice a que grupo pertenece. */
  exportarCsv(): void {
    const r = this.reporte();
    if (!r) {
      return;
    }
    const agrupado = r.grupos.some((g) => g.etiqueta !== null);
    const encabezado = [...(agrupado ? ['Grupo'] : []), ...r.columnas.map((c) => c.titulo)];
    const filas = r.grupos.flatMap((g) =>
      g.filas.map((f) => [
        ...(agrupado ? [g.etiqueta] : []),
        ...r.columnas.map((c) => this.valorCsv(c, f)),
      ]),
    );
    descargarCsv(this.config.archivo, encabezado, filas);
  }

  private valorCsv(c: ColumnaReporte, f: FilaReporte): string | number | null {
    const valor = f.valores[c.clave];
    if (valor === null || valor === undefined) {
      return null;
    }
    if (c.tipo === 'MONTO' || c.tipo === 'NUMERO') {
      return Number(valor);
    }
    return c.tipo === 'TEXTO' ? String(valor) : this.formatear(c, valor);
  }

  formatear(c: ColumnaReporte, valor: string | number | null | undefined): string {
    if (valor === null || valor === undefined || valor === '') {
      return '';
    }
    switch (c.tipo) {
      case 'MONTO':
        return 'Q' + this.decimalPipe.transform(valor, '1.2-2');
      case 'NUMERO':
        return this.decimalPipe.transform(valor, '1.0-2') ?? '';
      case 'FECHA':
        return this.datePipe.transform(String(valor), 'dd/MM/yyyy') ?? '';
      case 'FECHA_HORA': {
        // Un pago registrado solo con el dia queda a las 00:00: se muestra sin hora.
        const texto = String(valor);
        const sinHora = /T00:00(:00)?$/.test(texto);
        return this.datePipe.transform(texto, sinHora ? 'dd/MM/yyyy' : 'dd/MM/yyyy HH:mm') ?? '';
      }
      default:
        return String(valor);
    }
  }

  esNumerica(c: ColumnaReporte): boolean {
    return c.tipo === 'MONTO' || c.tipo === 'NUMERO';
  }

  get columnasQueSuman(): ColumnaReporte[] {
    return this.reporte()?.columnas.filter((c) => c.sumar) ?? [];
  }

  /** Posicion de la primera columna que suma: el rotulo "Subtotal" va en la celda anterior. */
  get columnasAntesDeLaSuma(): number {
    const columnas = this.reporte()?.columnas ?? [];
    const indice = columnas.findIndex((c) => c.sumar);
    return indice < 0 ? columnas.length : indice;
  }

  get agrupado(): boolean {
    return (this.reporte()?.grupos ?? []).some((g) => g.etiqueta !== null);
  }
}
