package com.kardex.application.ports.input.kardex;

import com.kardex.domain.model.Kardex;

/**
 * @brief Input port for Kardex command operations
 * 
 * Defines the contract for processing inventory movement commands including
 * purchases, sales, and their respective returns.
 */
public interface IKardexCommandPort {
    /**
     * @brief Registers a purchase transaction
     * @param kardex Purchase details to register
     * @return Processed kardex with updated balances
     */
    Kardex registerPurchase(Kardex kardex);
    
    /**
     * @brief Registers a sale transaction
     * @param kardex Sale details to register
     * @return Processed kardex with updated balances
     */
    Kardex registerSale(Kardex kardex);
    
    /**
     * @brief Registers a purchase return transaction
     * @param kardex Return details to register
     * @return Processed kardex with updated balances
     */
    Kardex registerReturnOnPurchase(Kardex kardex);
    
    /**
     * @brief Registers a sale return transaction
     * @param kardex Return details to register
     * @return Processed kardex with updated balances
     */
    Kardex registerReturnOnSale(Kardex kardex);
    
    /**
     * @brief Deletes all kardex records
     */
    void deleteAll();
}
