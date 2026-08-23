import { MatPaginatorIntl } from '@angular/material/paginator';

export function crearPaginatorIntlEspanol(): MatPaginatorIntl {
  const intl = new MatPaginatorIntl();

  intl.itemsPerPageLabel = 'Elementos por página:';
  intl.nextPageLabel = 'Página siguiente';
  intl.previousPageLabel = 'Página anterior';
  intl.firstPageLabel = 'Primera página';
  intl.lastPageLabel = 'Última página';

  intl.getRangeLabel = (page: number, pageSize: number, length: number): string => {
    if (length === 0 || pageSize === 0) {
      return `0 de ${length}`;
    }
    const total = Math.max(length, 0);
    const inicio = page * pageSize;
    const fin = inicio < total ? Math.min(inicio + pageSize, total) : inicio + pageSize;
    return `${inicio + 1} - ${fin} de ${total}`;
  };

  return intl;
}
