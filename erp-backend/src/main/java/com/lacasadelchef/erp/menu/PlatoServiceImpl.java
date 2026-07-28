package com.lacasadelchef.erp.menu;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Plato;
import com.lacasadelchef.erp.menu.dto.PlatoRequest;
import com.lacasadelchef.erp.menu.dto.PlatoResponse;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.PlatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlatoServiceImpl implements PlatoService {

    private static final String TABLA = "plato";

    private final PlatoRepository platoRepository;
    private final EstadoRepository estadoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public Page<PlatoResponse> listar(String nombre, Pageable pageable) {
        Page<Plato> page = (nombre == null || nombre.isBlank())
                ? platoRepository.findAll(pageable)
                : platoRepository.findByNombrePlatoContainingIgnoreCase(nombre.trim(), pageable);
        return page.map(PlatoResponse::desde);
    }

    @Override
    @Transactional(readOnly = true)
    public PlatoResponse obtenerPorId(Integer id) {
        return PlatoResponse.desde(buscarPlato(id));
    }

    @Override
    @Transactional
    public PlatoResponse crear(PlatoRequest request) {
        Plato plato = new Plato();
        aplicar(request, plato);
        plato = platoRepository.save(plato);
        bitacoraMovimientoService.registrar(TABLA, plato.getIdPlato(), Operacion.INSERT);
        return PlatoResponse.desde(plato);
    }

    @Override
    @Transactional
    public PlatoResponse actualizar(Integer id, PlatoRequest request) {
        Plato plato = buscarPlato(id);
        aplicar(request, plato);
        plato = platoRepository.save(plato);
        bitacoraMovimientoService.registrar(TABLA, plato.getIdPlato(), Operacion.UPDATE);
        return PlatoResponse.desde(plato);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        // Si el plato esta en uso en algun menu, la FK lo impide y el
        // GlobalExceptionHandler lo traduce a HTTP 409.
        Plato plato = buscarPlato(id);
        platoRepository.delete(plato);
        bitacoraMovimientoService.registrar(TABLA, plato.getIdPlato(), Operacion.DELETE);
    }

    private Plato buscarPlato(Integer id) {
        return platoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plato", id));
    }

    private void aplicar(PlatoRequest request, Plato plato) {
        Estado estado = estadoRepository.findById(request.idEstado())
                .orElseThrow(() -> new ResourceNotFoundException("Estado", request.idEstado()));
        plato.setNombrePlato(request.nombrePlato().trim());
        plato.setEstado(estado);
    }
}
