package com.kardex.domain.port;

import java.util.List;

import com.kardex.domain.model.Product;

/**
 * @brief Output port for Product write operations
 * 
 * Defines contract for persisting product data with
 * batch processing capabilities for efficient synchronization.
 */
public interface IProductCommandRepositoryPort {
     /**
      * @brief Saves multiple products in batch operation
      * @param products List of products to save or update
      * @return Status message indicating operation result
      */
     String saveAll(List<Product> products);
     
     /**
      * @brief Saves a single product
      * @param product Product to save
      * @return Status message indicating operation result
      */
     String save(Product product);
}
