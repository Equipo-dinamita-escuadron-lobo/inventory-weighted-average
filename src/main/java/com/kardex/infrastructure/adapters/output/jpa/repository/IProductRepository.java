package com.kardex.infrastructure.adapters.output.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;

public interface IProductRepository extends JpaRepository<ProductEntity, Long> {
    
}
