package com.kardex.infrastructure.adapters.output.jpa.adapter;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.kardex.domain.model.Product;
import com.kardex.domain.port.IProductQueryRepositoryPort;
import com.kardex.infrastructure.adapters.output.jpa.mapper.IProductEntityQueryMapper;
import com.kardex.infrastructure.adapters.output.jpa.repository.IProductRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductQueryAdapter implements IProductQueryRepositoryPort {

    private final IProductRepository productRepository;
    private final IProductEntityQueryMapper productEntityMapper;

    @Override
    public List<Product> findAll(String enterpriseId) {
        return productRepository.findAllByEnterpriseId(enterpriseId).stream()
                .map(productEntityMapper::toDomain)
                .toList();  
    }

    @Override
    public boolean existsByIdProduct(Long id) {
        return productRepository.existsByIdProduct(id);
    }
    
    
}
