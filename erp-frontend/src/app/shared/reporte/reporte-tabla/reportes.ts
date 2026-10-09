import { inject } from '@angular/core';
import { map } from 'rxjs';
import { EventoService } from '../../../features/eventos/evento.service';
import { InventarioService } from '../../../features/inventario/inventario.service';
import { PagoService } from '../../../features/pagos/pago.service';
import { estadosDe, fijas } from './opciones';
import { ConfigReporte } from './reporte-config';

/*
 * Los reportes de cada modulo. Todos usan la misma pantalla (ReporteTablaPage); aqui solo
 * cambia el endpoint, los filtros y las agrupaciones propias de cada uno.
 */

export const REPORTE_CLIENTES: ConfigReporte = {
  titulo: 'Clientes registrados',
  subtitulo: 'Clientes dados de alta en el periodo.',
  icono: 'people',
  paginaUrl: '/api/clientes',
  url: 'clientes/reportes/clientes',
  archivo: 'clientes',
  fechas: { porDefecto: 'mes', etiqueta: 'Registrados desde' },
  agrupaciones: [
    { valor: 'ESTADO', etiqueta: 'Por estado' },
    { valor: 'DEPARTAMENTO', etiqueta: 'Por departamento' },
  ],
  filtros: [{ clave: 'idEstado', etiqueta: 'Estado', icono: 'toggle_on', tipo: 'lista', opciones: estadosDe('GENERAL') }],
  sinDatos: 'No hay clientes registrados en este periodo',
};

export const REPORTE_COTIZACIONES: ConfigReporte = {
  titulo: 'Cotizaciones por periodo',
  subtitulo: 'Cotizaciones con su versión vigente, su estado y su monto.',
  icono: 'request_quote',
  paginaUrl: '/api/cotizaciones',
  url: 'cotizaciones/reportes/cotizaciones',
  archivo: 'cotizaciones',
  fechas: { porDefecto: 'mes' },
  agrupaciones: [
    { valor: 'ESTADO', etiqueta: 'Por estado' },
    { valor: 'CLIENTE', etiqueta: 'Por cliente' },
  ],
  filtros: [
    { clave: 'idEstado', etiqueta: 'Estado', icono: 'flag', tipo: 'lista', opciones: estadosDe('COTIZACION') },
    { clave: 'idCliente', etiqueta: 'Cliente', icono: 'person', tipo: 'cliente' },
  ],
  sinDatos: 'No hay cotizaciones en este periodo',
};

export const REPORTE_EVENTOS: ConfigReporte = {
  titulo: 'Reporte de eventos',
  subtitulo: 'Eventos con su fecha, cliente, tipo, personas y estado.',
  icono: 'event_note',
  paginaUrl: '/api/eventos',
  url: 'eventos/reportes/eventos',
  archivo: 'eventos',
  fechas: { porDefecto: 'mes' },
  fechaSegun: [
    { valor: 'EVENTO', etiqueta: 'Fecha del evento' },
    { valor: 'REGISTRO', etiqueta: 'Fecha de registro' },
  ],
  agrupaciones: [
    { valor: 'ESTADO', etiqueta: 'Por estado' },
    { valor: 'TIPO', etiqueta: 'Por tipo de evento' },
  ],
  filtros: [
    { clave: 'idEstado', etiqueta: 'Estado', icono: 'flag', tipo: 'lista', opciones: estadosDe('EVENTO') },
    {
      clave: 'idTipoEvento',
      etiqueta: 'Tipo de evento',
      icono: 'celebration',
      tipo: 'lista',
      opciones: () =>
        inject(EventoService)
          .listarTiposEvento()
          .pipe(map((tipos) => tipos.map((t) => ({ valor: t.idTipoEvento, etiqueta: t.nombreTipo })))),
    },
    { clave: 'idCliente', etiqueta: 'Cliente', icono: 'person', tipo: 'cliente' },
  ],
  sinDatos: 'No hay eventos en este periodo',
};

export const REPORTE_MOVIMIENTOS: ConfigReporte = {
  titulo: 'Movimientos de inventario',
  subtitulo: 'Entradas, salidas y ajustes del periodo.',
  icono: 'swap_vert',
  paginaUrl: '/api/movimientos-inventario',
  url: 'movimientos-inventario/reportes/movimientos',
  archivo: 'movimientos_inventario',
  fechas: { porDefecto: 'mes' },
  agrupaciones: [
    { valor: 'PRODUCTO', etiqueta: 'Por producto' },
    { valor: 'TIPO', etiqueta: 'Por tipo de movimiento' },
  ],
  filtros: [
    {
      clave: 'tipoMovimiento',
      etiqueta: 'Tipo',
      icono: 'swap_vert',
      tipo: 'lista',
      opciones: fijas([
        { valor: 'ENTRADA', etiqueta: 'Entrada' },
        { valor: 'SALIDA', etiqueta: 'Salida' },
        { valor: 'AJUSTE', etiqueta: 'Ajuste' },
      ]),
    },
    {
      clave: 'idProducto',
      etiqueta: 'Producto',
      icono: 'inventory_2',
      tipo: 'lista',
      opciones: () =>
        inject(InventarioService)
          .listarProductos('', 0, 1000)
          .pipe(map((page) => page.content.map((p) => ({ valor: p.idProducto, etiqueta: p.nombreProducto })))),
    },
  ],
  sinDatos: 'No hay movimientos en este periodo',
};

export const REPORTE_RECIBOS: ConfigReporte = {
  titulo: 'Recibos emitidos',
  subtitulo: 'Pagos recibidos, en el orden en que se emitieron sus recibos.',
  icono: 'receipt_long',
  paginaUrl: '/api/pagos',
  url: 'pagos/reportes/recibos',
  archivo: 'recibos_emitidos',
  fechas: { porDefecto: 'mes' },
  agrupaciones: [{ valor: 'METODO', etiqueta: 'Por forma de pago' }],
  filtros: [
    {
      clave: 'idMetodoPago',
      etiqueta: 'Forma de pago',
      icono: 'payments',
      tipo: 'lista',
      todos: 'Todas',
      opciones: () =>
        inject(PagoService)
          .listarMetodos()
          .pipe(map((metodos) => metodos.map((m) => ({ valor: m.idMetodoPago, etiqueta: m.nombreMetodo })))),
    },
    { clave: 'idEstado', etiqueta: 'Estado', icono: 'flag', tipo: 'lista', opciones: estadosDe('PAGO') },
  ],
  sinDatos: 'No hay recibos emitidos en este periodo',
};

export const REPORTE_CUENTAS_POR_COBRAR: ConfigReporte = {
  titulo: 'Cuentas por cobrar',
  subtitulo: 'Eventos con saldo pendiente y lo que les toca pagar.',
  icono: 'account_balance_wallet',
  paginaUrl: '/api/pagos',
  url: 'pagos/reportes/cuentas-por-cobrar',
  archivo: 'cuentas_por_cobrar',
  fechas: { porDefecto: 'ninguna', etiqueta: 'Eventos desde' },
  agrupaciones: [
    { valor: 'SITUACION', etiqueta: 'Por situación' },
    { valor: 'CLIENTE', etiqueta: 'Por cliente' },
  ],
  filtros: [
    { clave: 'idCliente', etiqueta: 'Cliente', icono: 'person', tipo: 'cliente' },
    {
      clave: 'situacion',
      etiqueta: 'Situación',
      icono: 'flag',
      tipo: 'lista',
      todos: 'Todas',
      opciones: fijas([
        { valor: 'ANTICIPO', etiqueta: 'Anticipo pendiente' },
        { valor: 'SALDO_FINAL', etiqueta: 'Saldo al finalizar' },
        { valor: 'VENCIDO', etiqueta: 'Saldo vencido' },
      ]),
    },
  ],
  sinDatos: 'No hay eventos con saldo pendiente',
};

export const REPORTE_PAGO_PERSONAL: ConfigReporte = {
  titulo: 'Pago al personal',
  subtitulo: 'Lo pagado a cada persona en los eventos finalizados.',
  icono: 'badge',
  paginaUrl: '/api/rentabilidad',
  url: 'rentabilidad/reportes/pago-personal',
  archivo: 'pago_al_personal',
  fechas: { porDefecto: 'mes' },
  agrupaciones: [{ valor: 'PERSONA', etiqueta: 'Por persona' }],
  filtros: [],
  sinDatos: 'No hay pagos al personal en este periodo',
};
