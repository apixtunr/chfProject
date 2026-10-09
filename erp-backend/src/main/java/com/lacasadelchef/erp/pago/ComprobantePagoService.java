package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.pago.dto.ComprobantePagoRequest;
import com.lacasadelchef.erp.pago.dto.ComprobantePagoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ComprobantePagoService {

    /** El archivo de un comprobante, tal como se descarga. */
    record Archivo(String nombre, String tipoContenido, byte[] contenido) {
    }

    List<ComprobantePagoResponse> listar(Integer idPago);

    /** @param archivo foto o PDF del comprobante; puede venir null y agregarse despues */
    ComprobantePagoResponse agregar(Integer idPago, ComprobantePagoRequest request, MultipartFile archivo);

    ComprobantePagoResponse actualizar(Integer idPago, Integer idComprobante, ComprobantePagoRequest request);

    /** Agrega el archivo a un comprobante que no lo tenia, o lo reemplaza. */
    ComprobantePagoResponse guardarArchivo(Integer idPago, Integer idComprobante, MultipartFile archivo);

    Archivo obtenerArchivo(Integer idPago, Integer idComprobante);

    void eliminar(Integer idPago, Integer idComprobante);
}
