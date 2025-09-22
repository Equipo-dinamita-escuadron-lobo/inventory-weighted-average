package com.kardex.infrastructure.adapters.output.jpa.adapter;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.kardex.domain.model.Product;
import com.kardex.domain.port.IProductQueryRepositoryPort;
import com.kardex.infrastructure.adapters.output.jpa.mapper.IProductEntityQueryMapper;
import com.kardex.infrastructure.adapters.output.jpa.repository.IProductRepository;

import lombok.RequiredArgsConstructor;

/**
 * @brief JPA adapter for Product query operations
 * 
 * Implements the repository port for querying Product data from the database.
 * Handles enterprise-specific filtering and existence checks.
 */
@Repository
@RequiredArgsConstructor
public class ProductQueryAdapter implements IProductQueryRepositoryPort {

    private final IProductRepository productRepository;
    private final IProductEntityQueryMapper productEntityMapper;

    /**
     * @brief Finds all products for a specific enterprise
     * @param enterpriseId The enterprise identifier
     * @return List of products belonging to the enterprise
     */
    @Override
    public List<Product> findAll(String enterpriseId) {
        return productRepository.findAllByEnterpriseId(enterpriseId).stream()
                .map(productEntityMapper::toDomain)
                .toList();  
    }

    /**
     * @brief Checks if a product exists by its ID
     * @param id The product identifier
     * @return True if product exists, false otherwise
     */
    @Override
    public boolean existsByProductId(Long id) {
        return productRepository.existsByProductId(id);
    }
    
    
}
