package com.kardex.application.ports.input.product;

import java.util.List;

import com.kardex.domain.model.Product;

/**
 * @brief Input port for product query operations
 * 
 * Provides read access to product information within
 * an enterprise context.
 */
public interface IProductQueryPort {
    /**
     * @brief Retrieves all products for an enterprise
     * @param enterpriseId Enterprise identifier
     * @return List of products belonging to the enterprise
     */
    List<Product> findAll(String enterpriseId);
}
