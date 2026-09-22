package com.lacasadelchef.erp.cliente;

import com.lacasadelchef.erp.cliente.dto.ClienteRequest;
import com.lacasadelchef.erp.cliente.dto.ClienteResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.entity.Municipio;
import com.lacasadelchef.erp.repository.ClienteRepository;
import com.lacasadelchef.erp.repository.MunicipioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private static final String TABLA = "cliente";

    private final ClienteRepository clienteRepository;
    private final MunicipioRepository municipioRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(String nombre, Pageable pageable) {
        Page<Cliente> page = (nombre == null || nombre.isBlank())
                ? clienteRepository.findAll(pageable)
                : clienteRepository.findByNombreContainingIgnoreCase(nombre.trim(), pageable);
        return page.map(ClienteResponse::desde);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(Integer id) {
        return ClienteResponse.desde(buscarCliente(id));
    }

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        Cliente cliente = new Cliente();
        aplicar(request, cliente);
        cliente = clienteRepository.save(cliente);
        bitacoraMovimientoService.registrar(TABLA, cliente.getIdCliente(), Operacion.INSERT);
        return ClienteResponse.desde(cliente);
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Integer id, ClienteRequest request) {
        Cliente cliente = buscarCliente(id);
        aplicar(request, cliente);
        cliente = clienteRepository.save(cliente);
        return ClienteResponse.desde(cliente);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        // Si el cliente tiene cotizaciones/eventos, la FK lo impide y el
        // GlobalExceptionHandler lo traduce a HTTP 409.
        Cliente cliente = buscarCliente(id);
        clienteRepository.delete(cliente);
        bitacoraMovimientoService.registrar(TABLA, cliente.getIdCliente(), Operacion.DELETE);
    }

    private Cliente buscarCliente(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    private void aplicar(ClienteRequest request, Cliente cliente) {
        Municipio municipio = municipioRepository.findById(request.idMunicipio())
                .orElseThrow(() -> new ResourceNotFoundException("Municipio", request.idMunicipio()));
        cliente.setNombre(request.nombre().trim());
        cliente.setCorreo(request.correo());
        cliente.setTelefono(request.telefono());
        cliente.setNit(request.nit());
        cliente.setDireccion(request.direccion().trim());
        cliente.setMunicipio(municipio);
    }
}
