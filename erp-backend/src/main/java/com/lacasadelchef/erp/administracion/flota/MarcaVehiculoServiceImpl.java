package com.lacasadelchef.erp.administracion.flota;

import com.lacasadelchef.erp.administracion.flota.dto.MarcaVehiculoRequest;
import com.lacasadelchef.erp.administracion.flota.dto.MarcaVehiculoResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.MarcaVehiculo;
import com.lacasadelchef.erp.repository.MarcaVehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarcaVehiculoServiceImpl implements MarcaVehiculoService {

    private static final String TABLA = "marca_vehiculo";

    private final MarcaVehiculoRepository marcaVehiculoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<MarcaVehiculoResponse> listar() {
        return marcaVehiculoRepository.findAll().stream().map(MarcaVehiculoResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MarcaVehiculoResponse obtenerPorId(Integer id) {
        return MarcaVehiculoResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public MarcaVehiculoResponse crear(MarcaVehiculoRequest request) {
        MarcaVehiculo marca = new MarcaVehiculo();
        aplicar(request, marca);
        marca = marcaVehiculoRepository.save(marca);
        bitacoraMovimientoService.registrar(TABLA, marca.getIdMarcaVehiculo(), Operacion.INSERT);
        return MarcaVehiculoResponse.desde(marca);
    }

    @Override
    @Transactional
    public MarcaVehiculoResponse actualizar(Integer id, MarcaVehiculoRequest request) {
        MarcaVehiculo marca = buscar(id);
        aplicar(request, marca);
        marca = marcaVehiculoRepository.save(marca);
        bitacoraMovimientoService.registrar(TABLA, marca.getIdMarcaVehiculo(), Operacion.UPDATE);
        return MarcaVehiculoResponse.desde(marca);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        MarcaVehiculo marca = buscar(id);
        marcaVehiculoRepository.delete(marca);
        bitacoraMovimientoService.registrar(TABLA, marca.getIdMarcaVehiculo(), Operacion.DELETE);
    }

    private MarcaVehiculo buscar(Integer id) {
        return marcaVehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MarcaVehiculo", id));
    }

    private void aplicar(MarcaVehiculoRequest request, MarcaVehiculo marca) {
        marca.setNombreMarca(request.nombreMarca().trim());
    }
}
