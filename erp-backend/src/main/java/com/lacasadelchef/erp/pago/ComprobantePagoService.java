package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.pago.dto.ComprobantePagoRequest;
import com.lacasadelchef.erp.pago.dto.ComprobantePagoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ComprobantePagoService {

    record Archivo(String nombre, String tipoContenido, byte[] contenido) {
    }

    List<ComprobantePagoResponse> listar(Integer idPago);

    ComprobantePagoResponse agregar(Integer idPago, ComprobantePagoRequest request, MultipartFile archivo);

    ComprobantePagoResponse actualizar(Integer idPago, Integer idComprobante, ComprobantePagoRequest request);

    ComprobantePagoResponse guardarArchivo(Integer idPago, Integer idComprobante, MultipartFile archivo);

    Archivo obtenerArchivo(Integer idPago, Integer idComprobante);

    void eliminar(Integer idPago, Integer idComprobante);
}
