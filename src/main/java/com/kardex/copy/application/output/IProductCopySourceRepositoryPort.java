package com.kardex.copy.application.output;

import com.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;

import java.util.List;

/**
 * Puerto de salida: lectura de ProductEntity del tenant origen para copia.
 * REQ-INVWA-01.
 */
public interface IProductCopySourceRepositoryPort {

    List<ProductEntity> findByEnterpriseId(String enterpriseId);
}
