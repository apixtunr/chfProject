package com.lacasadelchef.erp.administracion.flota;

import com.lacasadelchef.erp.administracion.flota.dto.LineaVehiculoRequest;
import com.lacasadelchef.erp.administracion.flota.dto.LineaVehiculoResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.LineaVehiculo;
import com.lacasadelchef.erp.entity.MarcaVehiculo;
import com.lacasadelchef.erp.repository.LineaVehiculoRepository;
import com.lacasadelchef.erp.repository.MarcaVehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LineaVehiculoServiceImpl implements LineaVehiculoService {

    private static final String TABLA = "linea_vehiculo";

    private final LineaVehiculoRepository lineaVehiculoRepository;
    private final MarcaVehiculoRepository marcaVehiculoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<LineaVehiculoResponse> listar(Integer idMarcaVehiculo) {
        List<LineaVehiculo> lineas = (idMarcaVehiculo == null)
                ? lineaVehiculoRepository.findAll()
                : lineaVehiculoRepository.findByMarcaVehiculoIdMarcaVehiculo(idMarcaVehiculo);
        return lineas.stream().map(LineaVehiculoResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LineaVehiculoResponse obtenerPorId(Integer id) {
        return LineaVehiculoResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public LineaVehiculoResponse crear(LineaVehiculoRequest request) {
        LineaVehiculo linea = new LineaVehiculo();
        aplicar(request, linea);
        linea = lineaVehiculoRepository.save(linea);
        bitacoraMovimientoService.registrar(TABLA, linea.getIdLineaVehiculo(), Operacion.INSERT);
        return LineaVehiculoResponse.desde(linea);
    }

    @Override
    @Transactional
    public LineaVehiculoResponse actualizar(Integer id, LineaVehiculoRequest request) {
        LineaVehiculo linea = buscar(id);
        aplicar(request, linea);
        linea = lineaVehiculoRepository.save(linea);
        bitacoraMovimientoService.registrar(TABLA, linea.getIdLineaVehiculo(), Operacion.UPDATE);
        return LineaVehiculoResponse.desde(linea);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        LineaVehiculo linea = buscar(id);
        lineaVehiculoRepository.delete(linea);
        bitacoraMovimientoService.registrar(TABLA, linea.getIdLineaVehiculo(), Operacion.DELETE);
    }

    private LineaVehiculo buscar(Integer id) {
        return lineaVehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LineaVehiculo", id));
    }

    private void aplicar(LineaVehiculoRequest request, LineaVehiculo linea) {
        MarcaVehiculo marca = marcaVehiculoRepository.findById(request.idMarcaVehiculo())
                .orElseThrow(() -> new ResourceNotFoundException("MarcaVehiculo", request.idMarcaVehiculo()));
        linea.setMarcaVehiculo(marca);
        linea.setNombreLinea(request.nombreLinea().trim());
    }
}
