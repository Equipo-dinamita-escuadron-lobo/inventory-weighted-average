package com.kardex.domain.model;


public enum MovementType {
    PURCHASE("Compra"),
    SALE("Venta"),
    SALESRETURN("Devolución de venta"),
    PURCHASERETURN("Devolución de compra"),
    ADJUSTMENT("Ajuste");

    private final String description;

    MovementType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
