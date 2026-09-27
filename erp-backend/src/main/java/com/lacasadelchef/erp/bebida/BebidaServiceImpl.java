package com.lacasadelchef.erp.bebida;

import com.lacasadelchef.erp.bebida.dto.BebidaRequest;
import com.lacasadelchef.erp.bebida.dto.BebidaResponse;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Bebida;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.repository.BebidaRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Catalogo de bebidas que pueden incluir los platos. Igual que un plato: no se borra si
 * ya esta en uso (en un plato o en una cotizacion); se inactiva, y deja de ofrecerse al
 * cotizar sin cambiar lo que ya se cotizo.
 */
@Service
@RequiredArgsConstructor
public class BebidaServiceImpl implements BebidaService {

    private static final String ESTADO_ACTIVO = "ACTIVO";

    private final BebidaRepository bebidaRepository;
    private final EstadoRepository estadoRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<BebidaResponse> listar(String nombre, Pageable pageable) {
        Page<Bebida> page = (nombre == null || nombre.isBlank())
                ? bebidaRepository.findAll(pageable)
                : bebidaRepository.findByNombreBebidaContainingIgnoreCase(nombre.trim(), pageable);
        return page.map(BebidaResponse::desde);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BebidaResponse> listarActivas() {
        return bebidaRepository.findByEstadoNombreOrderByNombreBebida(ESTADO_ACTIVO).stream()
                .map(BebidaResponse::desde)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BebidaResponse obtenerPorId(Integer id) {
        return BebidaResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public BebidaResponse crear(BebidaRequest request) {
        String nombre = request.nombreBebida().trim();
        if (bebidaRepository.existsByNombreBebidaIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe la bebida '%s'".formatted(nombre));
        }
        Bebida bebida = new Bebida();
        aplicar(request, bebida);
        return BebidaResponse.desde(bebidaRepository.save(bebida));
    }

    @Override
    @Transactional
    public BebidaResponse actualizar(Integer id, BebidaRequest request) {
        Bebida bebida = buscar(id);
        String nombre = request.nombreBebida().trim();
        if (bebidaRepository.existsByNombreBebidaIgnoreCaseAndIdBebidaNot(nombre, id)) {
            throw new BusinessException("Ya existe la bebida '%s'".formatted(nombre));
        }
        aplicar(request, bebida);
        return BebidaResponse.desde(bebidaRepository.save(bebida));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Bebida bebida = buscar(id);
        long platos = bebidaRepository.contarPlatos(id);
        long lineas = bebidaRepository.contarLineas(id);
        if (platos > 0 || lineas > 0) {
            throw new BusinessException(("No se puede eliminar '%s': la incluyen %d plato(s) y la eligieron %d"
                    + " linea(s) de cotizaciones o eventos. Inactívela para que deje de ofrecerse.")
                    .formatted(bebida.getNombreBebida(), platos, lineas));
        }
        bebidaRepository.delete(bebida);
    }

    private Bebida buscar(Integer id) {
        return bebidaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bebida", id));
    }

    private void aplicar(BebidaRequest request, Bebida bebida) {
        Estado estado = estadoRepository.findById(request.idEstado())
                .orElseThrow(() -> new ResourceNotFoundException("Estado", request.idEstado()));
        bebida.setNombreBebida(request.nombreBebida().trim());
        bebida.setEstado(estado);
    }
}
