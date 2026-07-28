package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.GeneroRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.GeneroResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Genero;
import com.lacasadelchef.erp.repository.GeneroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GeneroServiceImpl implements GeneroService {

    private static final String TABLA = "genero";

    private final GeneroRepository generoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<GeneroResponse> listar() {
        return generoRepository.findAll().stream().map(GeneroResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneroResponse obtenerPorId(Integer id) {
        return GeneroResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public GeneroResponse crear(GeneroRequest request) {
        Genero genero = new Genero();
        aplicar(request, genero);
        genero = generoRepository.save(genero);
        bitacoraMovimientoService.registrar(TABLA, genero.getIdGenero(), Operacion.INSERT);
        return GeneroResponse.desde(genero);
    }

    @Override
    @Transactional
    public GeneroResponse actualizar(Integer id, GeneroRequest request) {
        Genero genero = buscar(id);
        aplicar(request, genero);
        genero = generoRepository.save(genero);
        bitacoraMovimientoService.registrar(TABLA, genero.getIdGenero(), Operacion.UPDATE);
        return GeneroResponse.desde(genero);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Genero genero = buscar(id);
        generoRepository.delete(genero);
        bitacoraMovimientoService.registrar(TABLA, genero.getIdGenero(), Operacion.DELETE);
    }

    private Genero buscar(Integer id) {
        return generoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genero", id));
    }

    private void aplicar(GeneroRequest request, Genero genero) {
        genero.setNombreGenero(request.nombreGenero().trim());
    }
}
