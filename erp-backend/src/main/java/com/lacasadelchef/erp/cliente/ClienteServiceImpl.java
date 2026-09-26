package com.lacasadelchef.erp.cliente;

import com.lacasadelchef.erp.cliente.dto.ClienteRequest;
import com.lacasadelchef.erp.cliente.dto.ClienteResponse;
import com.lacasadelchef.erp.cliente.dto.PosibleDuplicadoResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Municipio;
import com.lacasadelchef.erp.repository.ClienteRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.MunicipioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reglas de negocio de Clientes:
 * <ul>
 *   <li>NIT valido (digito verificador) y unico; vacio = "CF" (consumidor final).</li>
 *   <li>Al menos un medio de contacto: telefono o correo. Se pueden repetir entre
 *       clientes (una organizadora, una familia o una empresa con varias sucursales los
 *       comparten); la pantalla solo avisa, igual que con el nombre.</li>
 *   <li>No se borra: se inactiva. Uno inactivo no entra en cotizaciones ni eventos
 *       nuevos, pero su historial sigue visible. No se puede inactivar mientras tenga
 *       negocio abierto (cotizacion en curso, evento sin terminar o saldo pendiente).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private static final String TABLA = "cliente";
    private static final String TIPO_ESTADO_GENERAL = "GENERAL";
    private static final String ACTIVO = "ACTIVO";
    private static final String INACTIVO = "INACTIVO";

    private final ClienteRepository clienteRepository;
    private final MunicipioRepository municipioRepository;
    private final EstadoRepository estadoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(String busqueda, String estado, Pageable pageable) {
        String texto = busqueda == null ? "" : busqueda.trim();
        String digitos = texto.replaceAll("[\\s\\-]", "").toUpperCase(Locale.ROOT);
        String filtroEstado = estado == null ? "" : estado.trim().toUpperCase(Locale.ROOT);
        if (!filtroEstado.isEmpty() && !filtroEstado.equals(ACTIVO) && !filtroEstado.equals(INACTIVO)) {
            filtroEstado = ""; // "TODOS" o cualquier otro valor: sin filtro
        }
        return clienteRepository.buscar(texto, digitos, filtroEstado, pageable).map(ClienteResponse::desde);
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
        cliente.setEstado(buscarEstado(ACTIVO));
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
    public ClienteResponse cambiarEstado(Integer id, boolean activo) {
        Cliente cliente = buscarCliente(id);
        if (cliente.estaActivo() == activo) {
            return ClienteResponse.desde(cliente);
        }
        if (!activo) {
            validarSinNegocioAbierto(cliente);
        }
        cliente.setEstado(buscarEstado(activo ? ACTIVO : INACTIVO));
        // El cambio de estado lo registra la auditoria campo por campo (UPDATE de id_estado).
        return ClienteResponse.desde(clienteRepository.save(cliente));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PosibleDuplicadoResponse> posiblesDuplicados(String nombre, String nit, String telefono, String correo,
                                                             Integer idExcluir) {
        String nitNormalizado = NitGuatemala.normalizar(nit)
                .filter(n -> !NitGuatemala.CONSUMIDOR_FINAL.equals(n))
                .orElse("");
        String telefonoLimpio = telefono == null ? "" : telefono.replaceAll("[\\s\\-]", "");
        String correoLimpio = correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
        String nombreLimpio = nombre == null ? "" : nombre.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);

        List<PosibleDuplicadoResponse> resultado = new ArrayList<>();
        for (Cliente c : clienteRepository.buscarParecidos(nitNormalizado, telefonoLimpio, correoLimpio, nombreLimpio,
                idExcluir == null ? 0 : idExcluir)) {
            List<String> coincidencias = new ArrayList<>();
            if (!nitNormalizado.isEmpty() && nitNormalizado.equals(c.getNit())) coincidencias.add("NIT");
            if (!telefonoLimpio.isEmpty() && c.getTelefono() != null
                    && telefonoLimpio.equals(c.getTelefono().replaceAll("[\\s\\-]", ""))) coincidencias.add("teléfono");
            if (!correoLimpio.isEmpty() && correoLimpio.equalsIgnoreCase(c.getCorreo())) coincidencias.add("correo");
            if (!nombreLimpio.isEmpty() && nombreLimpio.equalsIgnoreCase(c.getNombre().trim())) coincidencias.add("nombre");
            resultado.add(new PosibleDuplicadoResponse(c.getIdCliente(), c.getNombre(), c.getNit(), c.getTelefono(),
                    c.getCorreo(), c.estaActivo(), coincidencias));
        }
        return resultado;
    }

    // ------------------------------------------------------------------ privados

    private void aplicar(ClienteRequest request, Cliente cliente) {
        Municipio municipio = municipioRepository.findById(request.idMunicipio())
                .orElseThrow(() -> new ResourceNotFoundException("Municipio", request.idMunicipio()));

        String correo = vacioANulo(request.correo());
        String telefono = vacioANulo(request.telefono());
        if (correo == null && telefono == null) {
            throw new BusinessException("Indique al menos un medio de contacto: teléfono o correo");
        }

        String nit = NitGuatemala.normalizar(request.nit())
                .orElseThrow(() -> new BusinessException(
                        "El NIT %s no es válido. Revise que esté bien escrito, por ejemplo 6769359-8"
                                .formatted(request.nit().trim())));

        // El NIT identifica al cliente para facturar: es el unico dato que no se repite.
        Integer idActual = cliente.getIdCliente() == null ? 0 : cliente.getIdCliente();
        if (!NitGuatemala.CONSUMIDOR_FINAL.equals(nit)) {
            clienteRepository.findByNit(nit)
                    .filter(otro -> !otro.getIdCliente().equals(idActual))
                    .ifPresent(otro -> rechazarRepetido("El NIT " + nit, otro));
        }

        cliente.setNombre(request.nombre().trim().replaceAll("\\s+", " "));
        cliente.setCorreo(correo == null ? null : correo.toLowerCase(Locale.ROOT));
        cliente.setTelefono(telefono);
        cliente.setNit(nit);
        cliente.setDireccion(request.direccion().trim());
        cliente.setMunicipio(municipio);
    }

    private static void rechazarRepetido(String dato, Cliente otro) {
        throw new BusinessException(otro.estaActivo()
                ? "%s ya está registrado para %s".formatted(dato, otro.getNombre())
                : "%s ya está registrado para %s, que está inactivo: reactívelo en lugar de crear otro cliente"
                        .formatted(dato, otro.getNombre()));
    }

    private void validarSinNegocioAbierto(Cliente cliente) {
        Integer id = cliente.getIdCliente();
        List<String> pendientes = new ArrayList<>();
        long cotizaciones = clienteRepository.contarCotizacionesAbiertas(id);
        if (cotizaciones > 0) {
            pendientes.add(cotizaciones == 1 ? "1 cotización en curso" : cotizaciones + " cotizaciones en curso");
        }
        long eventos = clienteRepository.contarEventosVigentes(id);
        if (eventos > 0) {
            pendientes.add(eventos == 1 ? "1 evento sin terminar" : eventos + " eventos sin terminar");
        }
        BigDecimal saldo = clienteRepository.saldoPendiente(id);
        if (saldo != null && saldo.signum() > 0) {
            pendientes.add("Q %,.2f pendientes de cobro".formatted(saldo.setScale(2, RoundingMode.HALF_UP)));
        }
        if (!pendientes.isEmpty()) {
            throw new BusinessException("No se puede inactivar a %s: tiene %s"
                    .formatted(cliente.getNombre(), String.join(", ", pendientes)));
        }
    }

    private Cliente buscarCliente(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    private Estado buscarEstado(String nombre) {
        return estadoRepository.findByTipoEstadoNombreTipoAndNombre(TIPO_ESTADO_GENERAL, nombre)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el estado %s/%s (revisar datos semilla)".formatted(TIPO_ESTADO_GENERAL, nombre)));
    }

    private static String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
