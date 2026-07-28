package com.lacasadelchef.erp.cliente;

import com.lacasadelchef.erp.cliente.dto.UbicacionRequest;
import com.lacasadelchef.erp.cliente.dto.UbicacionResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.entity.Municipio;
import com.lacasadelchef.erp.entity.Ubicacion;
import com.lacasadelchef.erp.repository.ClienteRepository;
import com.lacasadelchef.erp.repository.MunicipioRepository;
import com.lacasadelchef.erp.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UbicacionServiceImpl implements UbicacionService {

    private static final String TABLA = "ubicacion";

    private final UbicacionRepository ubicacionRepository;
    private final ClienteRepository clienteRepository;
    private final MunicipioRepository municipioRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<UbicacionResponse> listar(Integer idCliente) {
        List<Ubicacion> ubicaciones = (idCliente == null)
                ? ubicacionRepository.findAll()
                : ubicacionRepository.findByClienteIdCliente(idCliente);
        return ubicaciones.stream().map(UbicacionResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UbicacionResponse obtenerPorId(Integer id) {
        return UbicacionResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public UbicacionResponse crear(UbicacionRequest request) {
        Ubicacion ubicacion = new Ubicacion();
        aplicar(request, ubicacion);
        ubicacion = ubicacionRepository.save(ubicacion);
        bitacoraMovimientoService.registrar(TABLA, ubicacion.getIdUbicacion(), Operacion.INSERT);
        return UbicacionResponse.desde(ubicacion);
    }

    @Override
    @Transactional
    public UbicacionResponse actualizar(Integer id, UbicacionRequest request) {
        Ubicacion ubicacion = buscar(id);
        aplicar(request, ubicacion);
        ubicacion = ubicacionRepository.save(ubicacion);
        bitacoraMovimientoService.registrar(TABLA, ubicacion.getIdUbicacion(), Operacion.UPDATE);
        return UbicacionResponse.desde(ubicacion);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        // Si la ubicacion tiene eventos asociados, la FK lo impide y el
        // GlobalExceptionHandler lo traduce a HTTP 409.
        Ubicacion ubicacion = buscar(id);
        ubicacionRepository.delete(ubicacion);
        bitacoraMovimientoService.registrar(TABLA, ubicacion.getIdUbicacion(), Operacion.DELETE);
    }

    private Ubicacion buscar(Integer id) {
        return ubicacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ubicacion", id));
    }

    private void aplicar(UbicacionRequest request, Ubicacion ubicacion) {
        Cliente cliente = null;
        if (request.idCliente() != null) {
            cliente = clienteRepository.findById(request.idCliente())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.idCliente()));
        }
        Municipio municipio = municipioRepository.findById(request.idMunicipio())
                .orElseThrow(() -> new ResourceNotFoundException("Municipio", request.idMunicipio()));

        ubicacion.setCliente(cliente);
        ubicacion.setMunicipio(municipio);
        ubicacion.setDireccion(request.direccion().trim());
    }
}
