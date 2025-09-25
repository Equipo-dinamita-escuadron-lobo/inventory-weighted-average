package com.kardex.application.service.product.query;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kardex.application.ports.input.IProductQueryPort;
import com.kardex.domain.model.Product;
import com.kardex.domain.port.IProductQueryRepositoryPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Service implementation for product query operations
 * 
 * Provides read access to product catalog within enterprise context.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductQueryService implements IProductQueryPort {

    private final IProductQueryRepositoryPort productQueryRepository;

    /**
     * @brief Retrieves all products for a specific enterprise
     * @param enterpriseId Enterprise identifier
     * @return List of products belonging to the enterprise
     */
    @Override
    public List<Product> findAll(String enterpriseId) {
        log.info("Retrieving all products for enterpriseId={}", enterpriseId);
        return productQueryRepository.findAll(enterpriseId);
    }
    
}
