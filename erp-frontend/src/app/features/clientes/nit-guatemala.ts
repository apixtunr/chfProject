import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Mismo calculo que NitGuatemala.java del backend: el verificador sale de multiplicar los
 * digitos del cuerpo, de derecha a izquierda, por 2, 3, 4...; (11 - suma % 11) % 11, y 10
 * se escribe K. Se valida tambien aqui solo para avisar mientras se escribe; la regla
 * que manda es la del backend.
 */
export function normalizarNit(nit: string | null | undefined): string | null {
  if (!nit || !nit.trim()) return 'CF';
  const limpio = nit.replace(/[\s.\-/]/g, '').toUpperCase();
  if (limpio === 'CF') return 'CF';
  if (!/^[0-9]{1,12}[0-9K]$/.test(limpio)) return null;
  const cuerpo = limpio.slice(0, -1);
  const verificador = limpio.slice(-1);
  let suma = 0;
  let factor = 2;
  for (let i = cuerpo.length - 1; i >= 0; i--) {
    suma += Number(cuerpo[i]) * factor++;
  }
  const resultado = (11 - (suma % 11)) % 11;
  const esperado = resultado === 10 ? 'K' : String(resultado);
  return verificador === esperado ? `${cuerpo}-${verificador}` : null;
}

export const nitValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null =>
  normalizarNit(control.value) === null ? { nit: true } : null;

/** Validador de grupo: al menos uno de los dos controles con valor. */
export function alMenosUnoValidator(a: string, b: string): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const va = grupo.get(a)?.value;
    const vb = grupo.get(b)?.value;
    return (va && String(va).trim()) || (vb && String(vb).trim()) ? null : { contacto: true };
  };
}
