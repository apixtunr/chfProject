package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.ComprobanteArchivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ComprobanteArchivoRepository extends JpaRepository<ComprobanteArchivo, Integer> {

    interface ArchivoResumen {
        Integer getIdComprobante();

        String getNombreArchivo();

        String getTipoContenido();

        Integer getTamanoBytes();
    }

    List<ArchivoResumen> findByIdComprobanteIn(Collection<Integer> idsComprobante);
}
