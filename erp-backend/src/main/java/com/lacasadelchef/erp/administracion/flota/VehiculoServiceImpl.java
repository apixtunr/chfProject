package com.lacasadelchef.erp.administracion.flota;

import com.lacasadelchef.erp.administracion.flota.dto.VehiculoRequest;
import com.lacasadelchef.erp.administracion.flota.dto.VehiculoResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.LineaVehiculo;
import com.lacasadelchef.erp.entity.TipoPlaca;
import com.lacasadelchef.erp.entity.Vehiculo;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.LineaVehiculoRepository;
import com.lacasadelchef.erp.repository.TipoPlacaRepository;
import com.lacasadelchef.erp.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehiculoServiceImpl implements VehiculoService {

    private static final String TABLA = "vehiculo";

    private final VehiculoRepository vehiculoRepository;
    private final LineaVehiculoRepository lineaVehiculoRepository;
    private final TipoPlacaRepository tipoPlacaRepository;
    private final EstadoRepository estadoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public Page<VehiculoResponse> listar(Pageable pageable) {
        return vehiculoRepository.findAll(pageable).map(VehiculoResponse::desde);
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponse obtenerPorId(Integer id) {
        return VehiculoResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public VehiculoResponse crear(VehiculoRequest request) {
        Vehiculo vehiculo = new Vehiculo();
        aplicar(request, vehiculo);
        vehiculo = vehiculoRepository.save(vehiculo);
        bitacoraMovimientoService.registrar(TABLA, vehiculo.getIdVehiculo(), Operacion.INSERT);
        return VehiculoResponse.desde(vehiculo);
    }

    @Override
    @Transactional
    public VehiculoResponse actualizar(Integer id, VehiculoRequest request) {
        Vehiculo vehiculo = buscar(id);
        aplicar(request, vehiculo);
        vehiculo = vehiculoRepository.save(vehiculo);
        bitacoraMovimientoService.registrar(TABLA, vehiculo.getIdVehiculo(), Operacion.UPDATE);
        return VehiculoResponse.desde(vehiculo);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        // Si el vehiculo tiene asignaciones a eventos, la FK lo impide y el
        // GlobalExceptionHandler lo traduce a HTTP 409.
        Vehiculo vehiculo = buscar(id);
        vehiculoRepository.delete(vehiculo);
        bitacoraMovimientoService.registrar(TABLA, vehiculo.getIdVehiculo(), Operacion.DELETE);
    }

    private Vehiculo buscar(Integer id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo", id));
    }

    private void aplicar(VehiculoRequest request, Vehiculo vehiculo) {
        LineaVehiculo lineaVehiculo = lineaVehiculoRepository.findById(request.idLineaVehiculo())
                .orElseThrow(() -> new ResourceNotFoundException("LineaVehiculo", request.idLineaVehiculo()));
        TipoPlaca tipoPlaca = tipoPlacaRepository.findById(request.idTipoPlaca())
                .orElseThrow(() -> new ResourceNotFoundException("TipoPlaca", request.idTipoPlaca()));
        Estado estado = estadoRepository.findById(request.idEstado())
                .orElseThrow(() -> new ResourceNotFoundException("Estado", request.idEstado()));

        vehiculo.setLineaVehiculo(lineaVehiculo);
        vehiculo.setTipoPlaca(tipoPlaca);
        vehiculo.setEstado(estado);
        vehiculo.setPlaca(request.placa().trim().toUpperCase());
        vehiculo.setAnioVehiculo(request.anioVehiculo());
    }
}
