package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.pago.dto.ComprobantePagoRequest;
import com.lacasadelchef.erp.pago.dto.ComprobantePagoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/pagos/{idPago}/comprobantes")
@RequiredArgsConstructor
public class ComprobantePagoController {

    private final ComprobantePagoService comprobantePagoService;

    @GetMapping
    public List<ComprobantePagoResponse> listar(@PathVariable Integer idPago) {
        return comprobantePagoService.listar(idPago);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/pagos', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public ComprobantePagoResponse agregar(@PathVariable Integer idPago,
                                           @Valid @RequestBody ComprobantePagoRequest request) {
        return comprobantePagoService.agregar(idPago, request, null);
    }

    /** Datos y archivo en una sola peticion: "datos" (JSON) y "archivo" (PDF, JPG o PNG). */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/pagos', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public ComprobantePagoResponse agregarConArchivo(@PathVariable Integer idPago,
                                                     @Valid @RequestPart("datos") ComprobantePagoRequest request,
                                                     @RequestPart(value = "archivo", required = false) MultipartFile archivo) {
        return comprobantePagoService.agregar(idPago, request, archivo);
    }

    @PutMapping("/{idComprobante}")
    @PreAuthorize("@permisoService.tienePermiso('/api/pagos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public ComprobantePagoResponse actualizar(@PathVariable Integer idPago, @PathVariable Integer idComprobante,
                                              @Valid @RequestBody ComprobantePagoRequest request) {
        return comprobantePagoService.actualizar(idPago, idComprobante, request);
    }

    /** Adjuntar el archivo es parte de registrar el comprobante: basta con poder dar de alta o modificar. */
    @PutMapping(path = "/{idComprobante}/archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@permisoService.tienePermiso('/api/pagos', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)"
            + " or @permisoService.tienePermiso('/api/pagos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public ComprobantePagoResponse guardarArchivo(@PathVariable Integer idPago, @PathVariable Integer idComprobante,
                                                  @RequestPart("archivo") MultipartFile archivo) {
        return comprobantePagoService.guardarArchivo(idPago, idComprobante, archivo);
    }

    /**
     * El archivo para verlo en el navegador. Se pide con el token como cualquier otra
     * llamada; no hay enlace publico. No se guarda en cache: es un documento de pago.
     */
    @GetMapping("/{idComprobante}/archivo")
    public ResponseEntity<byte[]> archivo(@PathVariable Integer idPago, @PathVariable Integer idComprobante) {
        ComprobantePagoService.Archivo archivo = comprobantePagoService.obtenerArchivo(idPago, idComprobante);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.tipoContenido()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(archivo.nombre(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noStore())
                .body(archivo.contenido());
    }

    @DeleteMapping("/{idComprobante}")
    @PreAuthorize("@permisoService.tienePermiso('/api/pagos', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer idPago, @PathVariable Integer idComprobante) {
        comprobantePagoService.eliminar(idPago, idComprobante);
        return ResponseEntity.noContent().build();
    }
}
