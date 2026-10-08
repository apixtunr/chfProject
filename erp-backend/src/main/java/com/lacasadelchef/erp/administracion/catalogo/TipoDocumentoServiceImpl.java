package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.TipoDocumentoRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.TipoDocumentoResponse;
import com.lacasadelchef.erp.administracion.rrhh.DocumentosEmpleado;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.TipoDocumento;
import com.lacasadelchef.erp.repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoDocumentoServiceImpl implements TipoDocumentoService {

    private final TipoDocumentoRepository tipoDocumentoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TipoDocumentoResponse> listar() {
        return tipoDocumentoRepository.findAll().stream().map(TipoDocumentoResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TipoDocumentoResponse obtenerPorId(Integer id) {
        return TipoDocumentoResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public TipoDocumentoResponse crear(TipoDocumentoRequest request) {
        TipoDocumento tipoDocumento = new TipoDocumento();
        aplicar(request, tipoDocumento);
        tipoDocumento = tipoDocumentoRepository.save(tipoDocumento);
        return TipoDocumentoResponse.desde(tipoDocumento);
    }

    @Override
    @Transactional
    public TipoDocumentoResponse actualizar(Integer id, TipoDocumentoRequest request) {
        TipoDocumento tipoDocumento = buscar(id);
        if (DocumentosEmpleado.esDelSistema(tipoDocumento.getNombreTipo())
                && !tipoDocumento.getNombreTipo().equals(request.nombreTipo().trim())) {
            throw new BusinessException("El tipo %s lo usan reglas del sistema y no se puede renombrar"
                    .formatted(tipoDocumento.getNombreTipo()));
        }
        aplicar(request, tipoDocumento);
        tipoDocumento = tipoDocumentoRepository.save(tipoDocumento);
        return TipoDocumentoResponse.desde(tipoDocumento);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        TipoDocumento tipoDocumento = buscar(id);
        if (DocumentosEmpleado.esDelSistema(tipoDocumento.getNombreTipo())) {
            throw new BusinessException("El tipo %s lo usan reglas del sistema y no se puede eliminar"
                    .formatted(tipoDocumento.getNombreTipo()));
        }
        tipoDocumentoRepository.delete(tipoDocumento);
    }

    private TipoDocumento buscar(Integer id) {
        return tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoDocumento", id));
    }

    private void aplicar(TipoDocumentoRequest request, TipoDocumento tipoDocumento) {
        tipoDocumento.setNombreTipo(request.nombreTipo().trim());
    }
}
