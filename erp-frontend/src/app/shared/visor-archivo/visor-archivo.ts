import { Component, OnDestroy, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

export interface VisorArchivoData {
  titulo: string;
  nombre: string;
  archivo: Blob;
}

/**
 * Muestra un archivo (foto o PDF de un comprobante) dentro del sistema, en una ventana
 * con boton de cerrar.
 *
 * Antes se abria en otra pestana: en el celular la imagen reemplazaba al sistema y el
 * navegador no siempre dejaba regresar. Las imagenes se ven aqui mismo; los PDF tambien
 * en computadora, pero los navegadores del celular no muestran un PDF dentro de una
 * pagina, asi que ahi se ofrece descargarlo (se abre con el visor del telefono).
 */
@Component({
  selector: 'app-visor-archivo',
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>{{ data.titulo }}</h2>
    <mat-dialog-content class="visor-contenido">
      @if (esImagen) {
        <img [src]="urlSegura" [alt]="data.nombre" class="visor-imagen" />
      } @else if (esPdf && !esCelular) {
        <iframe [src]="urlSegura" [title]="data.nombre" class="visor-pdf"></iframe>
      } @else {
        <p class="visor-aviso">
          <mat-icon>picture_as_pdf</mat-icon>
          {{ data.nombre }}: descárguelo para verlo en el teléfono.
        </p>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <a mat-button [href]="urlSegura" [attr.download]="data.nombre">
        <mat-icon>download</mat-icon>
        Descargar
      </a>
      <button mat-flat-button color="primary" mat-dialog-close cdkFocusInitial>Cerrar</button>
    </mat-dialog-actions>
  `,
  styles: `
    .visor-contenido {
      display: flex;
      justify-content: center;
    }
    .visor-imagen {
      max-width: 100%;
      max-height: 70vh;
      object-fit: contain;
    }
    .visor-pdf {
      width: min(80vw, 900px);
      height: 70vh;
      border: 0;
    }
    .visor-aviso {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  `,
})
export class VisorArchivo implements OnDestroy {
  readonly data = inject<VisorArchivoData>(MAT_DIALOG_DATA);
  private readonly url = URL.createObjectURL(this.data.archivo);
  readonly urlSegura: SafeResourceUrl = inject(DomSanitizer).bypassSecurityTrustResourceUrl(this.url);

  readonly esImagen = this.data.archivo.type.startsWith('image/');
  readonly esPdf = this.data.archivo.type === 'application/pdf';
  /** Pantalla tactil y angosta: ahi el PDF no se puede mostrar dentro de la pagina. */
  readonly esCelular = window.matchMedia('(pointer: coarse)').matches && window.innerWidth < 900;

  ngOnDestroy(): void {
    URL.revokeObjectURL(this.url);
  }
}
