package com.kardex.domain.model;

/**
 * @brief Enumeration of inventory movement types
 * 
 */
public enum MovementType {
    PURCHASE("Compra"),
    SALE("Venta"),
    SALESRETURN("Devolución de venta"),
    PURCHASERETURN("Devolución de compra");

    private final String description;

    MovementType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
