package com.kardex.copy.application.output;

import com.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;

/**
 * Puerto de salida: escritura de ProductEntity en tenant destino durante copia.
 * REQ-INVWA-01.
 */
public interface IProductCopyTargetRepositoryPort {

    ProductEntity guardar(ProductEntity entity);
}
