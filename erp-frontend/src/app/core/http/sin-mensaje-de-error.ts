import { HttpContextToken } from '@angular/common/http';

export const SIN_MENSAJE_DE_ERROR = new HttpContextToken<boolean>(() => false);
