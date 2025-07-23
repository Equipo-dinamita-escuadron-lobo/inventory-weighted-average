package com.kardex.infrastructure.adapters.output.jpa.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;

public interface IProductRepository extends JpaRepository<ProductEntity, Long> {

    Collection<ProductEntity> findAllByEnterpriseId(String enterpriseId);
    
}
