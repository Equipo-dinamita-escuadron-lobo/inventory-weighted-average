package com.kardex.copy.infrastructure.adapters.output.persistence.jpa;

import com.kardex.copy.application.output.IProductCopyTargetRepositoryPort;
import com.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;
import com.kardex.infrastructure.adapters.output.jpa.repository.IProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador JPA para IProductCopyTargetRepositoryPort.
 * REQ-INVWA-01.
 */
@Component
@RequiredArgsConstructor
public class ProductCopyTargetRepositoryAdapter implements IProductCopyTargetRepositoryPort {

    private final IProductRepository jpaRepository;

    @Override
    @Transactional
    public ProductEntity guardar(ProductEntity entity) {
        return jpaRepository.save(entity);
    }
}
