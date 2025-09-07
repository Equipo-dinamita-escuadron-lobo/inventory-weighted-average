package com.kardex.infrastructure.adapters.config.i18n;

/**
 * Constantes para las claves de mensajes internacionalizados.
 * Centraliza todas las claves de mensajes usadas en la aplicación.
 */
public final class MessageKeys {
    
    private MessageKeys() {
        // Clase de constantes, no debe ser instanciada
    }
    
    // Mensajes de error
    public static final String ERROR_PRODUCT_NOT_FOUND = "kardex.error.product.not.found";
    public static final String ERROR_BALANCE_UNIT_PRICE_ZERO = "kardex.error.balance.unit.price.zero";
    public static final String ERROR_RETURN_QUANTITY_EXCEEDED = "kardex.error.return.quantity.exceeded";
    public static final String ERROR_DUPLICATE_MOVEMENT = "kardex.error.duplicate.movement";
    public static final String ERROR_NO_ORIGINAL_MOVEMENT = "kardex.error.no.original.movement";
    public static final String ERROR_NO_PREVIOUS_KARDEX = "kardex.error.no.previous.kardex";
    public static final String ERROR_INVALID_MOVEMENT_TYPE = "kardex.error.invalid.movement.type";
    
    // Mensajes de log
    public static final String LOG_STOCK_REQUEST_SUCCESS = "kardex.log.stock.request.success";
    public static final String LOG_STOCK_REQUEST_ERROR = "kardex.log.stock.request.error";
}
