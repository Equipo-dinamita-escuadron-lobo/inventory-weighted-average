package com.kardex.application.ports.input.product;

/**
 * @brief Input port for product synchronization operations
 * 
 * Handles synchronization of product data from external sources
 * to maintain data consistency across systems.
 */
public interface IProductSyncCommandPort {
    /**
     * @brief Synchronizes products for a specific enterprise
     * @param enterpriseId Enterprise identifier to sync products for
     * @return Status message indicating sync result
     */
    String syncProductsByEnterpriseId(String enterpriseId);
}
