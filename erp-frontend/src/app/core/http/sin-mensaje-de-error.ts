import { HttpContextToken } from '@angular/common/http';

/** Peticiones de fondo que, si fallan, no deben mostrar un mensaje (un 401 cierra la sesion igual). */
export const SIN_MENSAJE_DE_ERROR = new HttpContextToken<boolean>(() => false);
