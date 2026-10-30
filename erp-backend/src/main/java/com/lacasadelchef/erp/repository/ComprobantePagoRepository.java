package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.ComprobantePago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComprobantePagoRepository extends JpaRepository<ComprobantePago, Integer> {

    List<ComprobantePago> findByPagoIdPago(Integer idPago);

    Optional<ComprobantePago> findByTipoComprobanteAndNumeroComprobante(String tipoComprobante, String numeroComprobante);
}
