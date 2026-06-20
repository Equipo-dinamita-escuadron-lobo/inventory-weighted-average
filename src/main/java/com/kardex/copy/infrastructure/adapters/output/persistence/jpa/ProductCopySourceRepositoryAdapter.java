package com.kardex.copy.infrastructure.adapters.output.persistence.jpa;

import com.kardex.copy.application.output.IProductCopySourceRepositoryPort;
import com.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;
import com.kardex.infrastructure.adapters.output.jpa.repository.IProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Adaptador JPA para IProductCopySourceRepositoryPort.
 * REQ-INVWA-01.
 */
@Component
@RequiredArgsConstructor
public class ProductCopySourceRepositoryAdapter implements IProductCopySourceRepositoryPort {

    private final IProductRepository jpaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductEntity> findByEnterpriseId(String enterpriseId) {
        return (List<ProductEntity>) jpaRepository.findAllByEnterpriseId(enterpriseId);
    }
}
