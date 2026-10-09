package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.ComprobanteArchivo;
import com.lacasadelchef.erp.entity.ComprobantePago;
import com.lacasadelchef.erp.entity.Pago;
import com.lacasadelchef.erp.pago.dto.ComprobantePagoRequest;
import com.lacasadelchef.erp.pago.dto.ComprobantePagoResponse;
import com.lacasadelchef.erp.repository.ComprobanteArchivoRepository;
import com.lacasadelchef.erp.repository.ComprobanteArchivoRepository.ArchivoResumen;
import com.lacasadelchef.erp.repository.ComprobantePagoRepository;
import com.lacasadelchef.erp.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComprobantePagoServiceImpl implements ComprobantePagoService {

    /** Los mismos de la restriccion ck_comprobante_pago_tipo (V32). */
    static final List<String> TIPOS_COMPROBANTE = List.of(
            "Boleta de depósito", "Voucher de tarjeta", "Comprobante de transferencia", "Factura", "Recibo");

    private static final String ESTADO_ANULADO = "ANULADO";

    private final ComprobantePagoRepository comprobantePagoRepository;
    private final ComprobanteArchivoRepository comprobanteArchivoRepository;
    private final PagoRepository pagoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ComprobantePagoResponse> listar(Integer idPago) {
        List<ComprobantePago> comprobantes = comprobantePagoRepository.findByPagoIdPago(idPago);
        if (comprobantes.isEmpty()) {
            return List.of();
        }
        // Los datos de los archivos en una sola consulta y sin su contenido.
        Map<Integer, ArchivoResumen> archivos = comprobanteArchivoRepository
                .findByIdComprobanteIn(comprobantes.stream().map(ComprobantePago::getIdComprobante).toList())
                .stream()
                .collect(Collectors.toMap(ArchivoResumen::getIdComprobante, Function.identity()));
        return comprobantes.stream()
                .map(c -> ComprobantePagoResponse.desde(c, archivos.get(c.getIdComprobante())))
                .toList();
    }

    /**
     * Comprobante y archivo van en la misma transaccion: si el archivo no es valido, el
     * comprobante tampoco queda grabado (sin eso quedaria uno "sin archivo" a medias).
     */
    @Override
    @Transactional
    public ComprobantePagoResponse agregar(Integer idPago, ComprobantePagoRequest request, MultipartFile archivo) {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", idPago));
        validarPagoVigente(pago);
        // El archivo se revisa antes de grabar nada: si no sirve, no queda el comprobante solo.
        ArchivoComprobante.Revisado revisado = archivo == null ? null : ArchivoComprobante.revisar(archivo);
        ComprobantePago comprobante = new ComprobantePago();
        comprobante.setPago(pago);
        aplicar(request, comprobante);
        comprobante = comprobantePagoRepository.save(comprobante);
        if (revisado == null) {
            return ComprobantePagoResponse.desde(comprobante);
        }
        return ComprobantePagoResponse.desde(comprobante, resumen(guardar(comprobante, revisado)));
    }

    @Override
    @Transactional
    public ComprobantePagoResponse actualizar(Integer idPago, Integer idComprobante, ComprobantePagoRequest request) {
        ComprobantePago comprobante = buscar(idPago, idComprobante);
        aplicar(request, comprobante);
        comprobante = comprobantePagoRepository.save(comprobante);
        return ComprobantePagoResponse.desde(comprobante,
                comprobanteArchivoRepository.findById(idComprobante).map(ComprobantePagoServiceImpl::resumen).orElse(null));
    }

    @Override
    @Transactional
    public ComprobantePagoResponse guardarArchivo(Integer idPago, Integer idComprobante, MultipartFile archivo) {
        ComprobantePago comprobante = buscar(idPago, idComprobante);
        validarPagoVigente(comprobante.getPago());
        return ComprobantePagoResponse.desde(comprobante, resumen(guardar(comprobante, ArchivoComprobante.revisar(archivo))));
    }

    @Override
    @Transactional(readOnly = true)
    public Archivo obtenerArchivo(Integer idPago, Integer idComprobante) {
        buscar(idPago, idComprobante);
        ComprobanteArchivo archivo = comprobanteArchivoRepository.findById(idComprobante)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El comprobante %d no tiene archivo".formatted(idComprobante)));
        return new Archivo(archivo.getNombreArchivo(), archivo.getTipoContenido(), archivo.getContenido());
    }

    @Override
    @Transactional
    public void eliminar(Integer idPago, Integer idComprobante) {
        ComprobantePago comprobante = buscar(idPago, idComprobante);
        // El archivo se borra primero y de forma explicita, para que quede en la bitacora.
        comprobanteArchivoRepository.findById(idComprobante).ifPresent(comprobanteArchivoRepository::delete);
        comprobantePagoRepository.delete(comprobante);
    }

    private ComprobanteArchivo guardar(ComprobantePago comprobante, ArchivoComprobante.Revisado revisado) {
        ComprobanteArchivo guardado = comprobanteArchivoRepository.findById(comprobante.getIdComprobante())
                .orElseGet(() -> {
                    ComprobanteArchivo nuevo = new ComprobanteArchivo();
                    nuevo.setComprobante(comprobante);
                    return nuevo;
                });
        guardado.setNombreArchivo(revisado.nombre());
        guardado.setTipoContenido(revisado.tipoContenido());
        guardado.setTamanoBytes(revisado.contenido().length);
        guardado.setContenido(revisado.contenido());
        guardado = comprobanteArchivoRepository.save(guardado);
        // archivo_url apunta a donde se descarga: el archivo vive en la base, no en un disco.
        comprobante.setArchivoUrl("/api/pagos/%d/comprobantes/%d/archivo"
                .formatted(comprobante.getPago().getIdPago(), comprobante.getIdComprobante()));
        comprobantePagoRepository.save(comprobante);
        return guardado;
    }

    private static ArchivoResumen resumen(ComprobanteArchivo archivo) {
        return new ArchivoResumen() {
            @Override
            public Integer getIdComprobante() {
                return archivo.getIdComprobante();
            }

            @Override
            public String getNombreArchivo() {
                return archivo.getNombreArchivo();
            }

            @Override
            public String getTipoContenido() {
                return archivo.getTipoContenido();
            }

            @Override
            public Integer getTamanoBytes() {
                return archivo.getTamanoBytes();
            }
        };
    }

    /** Un pago anulado ya no cuenta: respaldarlo con comprobantes no tiene sentido. */
    private static void validarPagoVigente(Pago pago) {
        if (ESTADO_ANULADO.equalsIgnoreCase(pago.getEstado().getNombre())) {
            throw new BusinessException("El pago #%d está anulado: no se le pueden agregar comprobantes"
                    .formatted(pago.getIdPago()));
        }
    }

    private ComprobantePago buscar(Integer idPago, Integer idComprobante) {
        ComprobantePago comprobante = comprobantePagoRepository.findById(idComprobante)
                .orElseThrow(() -> new ResourceNotFoundException("ComprobantePago", idComprobante));
        if (!comprobante.getPago().getIdPago().equals(idPago)) {
            throw new ResourceNotFoundException("ComprobantePago", idComprobante);
        }
        return comprobante;
    }

    private void aplicar(ComprobantePagoRequest request, ComprobantePago comprobante) {
        String tipo = request.tipoComprobante().trim();
        if (!TIPOS_COMPROBANTE.contains(tipo)) {
            throw new BusinessException("Tipo de comprobante no válido. Use uno de: " + String.join(", ", TIPOS_COMPROBANTE));
        }
        String numero = request.numeroComprobante().trim();
        // Una misma boleta no puede respaldar dos pagos.
        comprobantePagoRepository.findByTipoComprobanteAndNumeroComprobante(tipo, numero)
                .filter(otro -> !otro.getIdComprobante().equals(comprobante.getIdComprobante()))
                .ifPresent(otro -> {
                    throw new BusinessException("El comprobante %s %s ya respalda el pago #%d"
                            .formatted(tipo, numero, otro.getPago().getIdPago()));
                });
        comprobante.setNumeroComprobante(numero);
        comprobante.setTipoComprobante(tipo);
        comprobante.setFechaEmision(request.fechaEmision());
        comprobante.setEsValido(request.esValido() == null || request.esValido());
    }
}
