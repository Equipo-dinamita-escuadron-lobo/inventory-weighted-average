package com.kardex.copy.application.output;

import com.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;

import java.util.List;

/**
 * Puerto de salida: lectura de KardexEntity para un producto específico durante copia.
 * REQ-INVWA-01.
 */
public interface IKardexCopySourceRepositoryPort {

    List<KardexEntity> findByProductId(Long productId);
}
