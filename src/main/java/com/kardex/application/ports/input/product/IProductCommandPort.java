package com.kardex.application.ports.input.product;

public interface IProductCommandPort {
    /**
     * @brief Deletes a product by its ID and enterprise ID
    * @param productId Product ID to delete
    * @param enterpriseId Enterprise ID for context
    * @return Status message indicating operation result
    */
    String deleteById(Long productId, String enterpriseId);
    
    /**
     * @brief Deletes all products for a specific enterprise
    * @param enterpriseId Enterprise ID
    * @return Status message indicating operation result
    */
    String deleteAllByEnterpriseId(String enterpriseId);
}
