import { MatDateFormats } from '@angular/material/core';

export const FORMATO_FECHA_ES: MatDateFormats = {
  parse: {
    dateInput: null,
    timeInput: null,
  },
  display: {
    dateInput: { day: '2-digit', month: '2-digit', year: 'numeric' },
    monthYearLabel: { month: 'short', year: 'numeric' },
    dateA11yLabel: { day: '2-digit', month: '2-digit', year: 'numeric' },
    monthYearA11yLabel: { month: 'long', year: 'numeric' },
    timeInput: { hour: '2-digit', minute: '2-digit', hour12: false },
    timeOptionLabel: { hour: '2-digit', minute: '2-digit', hour12: false },
  },
};
