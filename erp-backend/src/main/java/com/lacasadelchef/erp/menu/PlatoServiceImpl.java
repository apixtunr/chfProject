package com.lacasadelchef.erp.menu;

import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Bebida;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Plato;
import com.lacasadelchef.erp.entity.PlatoBebida;
import com.lacasadelchef.erp.entity.UnidadVenta;
import com.lacasadelchef.erp.menu.dto.PlatoRequest;
import com.lacasadelchef.erp.menu.dto.PlatoResponse;
import com.lacasadelchef.erp.repository.BebidaRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.PlatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlatoServiceImpl implements PlatoService {

    private final PlatoRepository platoRepository;
    private final EstadoRepository estadoRepository;
    private final BebidaRepository bebidaRepository;

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
        return PlatoResponse.desde(plato);
    }

    @Override
    @Transactional
    public PlatoResponse actualizar(Integer id, PlatoRequest request) {
        Plato plato = buscarPlato(id);
        aplicar(request, plato);
        plato = platoRepository.save(plato);
        return PlatoResponse.desde(plato);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        // Si el plato esta en uso en algun menu, la FK lo impide y el
        // GlobalExceptionHandler lo traduce a HTTP 409.
        Plato plato = buscarPlato(id);
        platoRepository.delete(plato);
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
        plato.setUnidadVenta(request.unidadVenta() == null ? UnidadVenta.PERSONA : request.unidadVenta());
        asignarBebidas(plato, request.idsBebida());
    }

    /**
     * Deja en el plato exactamente las bebidas pedidas: quita las que ya no van y agrega
     * las nuevas (cada cambio queda en la bitacora como alta o baja en plato_bebida).
     */
    private void asignarBebidas(Plato plato, List<Integer> idsBebida) {
        Set<Integer> pedidas = idsBebida == null ? Set.of()
                : idsBebida.stream().filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        plato.getBebidas().removeIf(pb -> !pedidas.contains(pb.getBebida().getIdBebida()));
        Set<Integer> yaEstan = plato.getBebidas().stream()
                .map(pb -> pb.getBebida().getIdBebida())
                .collect(Collectors.toSet());
        for (Integer idBebida : pedidas) {
            if (yaEstan.contains(idBebida)) {
                continue;
            }
            Bebida bebida = bebidaRepository.findById(idBebida)
                    .orElseThrow(() -> new ResourceNotFoundException("Bebida", idBebida));
            plato.getBebidas().add(new PlatoBebida(plato, bebida));
        }
    }
}
