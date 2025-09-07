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
    public static final String LOG_PURCHASE_STARTED = "kardex.log.purchase.started";
    public static final String LOG_PURCHASE_COMPLETED = "kardex.log.purchase.completed";
    public static final String LOG_SALE_STARTED = "kardex.log.sale.started";
    public static final String LOG_SALE_COMPLETED = "kardex.log.sale.completed";
    public static final String LOG_PURCHASE_RETURN_STARTED = "kardex.log.purchase.return.started";
    public static final String LOG_PURCHASE_RETURN_COMPLETED = "kardex.log.purchase.return.completed";
    public static final String LOG_SALE_RETURN_STARTED = "kardex.log.sale.return.started";
    public static final String LOG_SALE_RETURN_COMPLETED = "kardex.log.sale.return.completed";
    public static final String LOG_VALIDATING_BUSINESS_RULES = "kardex.log.validating.business.rules";
    public static final String LOG_PRODUCT_EXISTS_VALIDATION = "kardex.log.product.exists.validation";
    
    // Mensajes de log para sincronización de productos
    public static final String LOG_SYNC_STARTED = "kardex.log.sync.started";
    public static final String LOG_SYNC_SUCCESS = "kardex.log.sync.success";
    public static final String LOG_SYNC_ERROR = "kardex.log.sync.error";
    public static final String LOG_NO_PREVIOUS_SYNC = "kardex.log.no.previous.sync";
    public static final String LOG_LAST_SYNC_DATE = "kardex.log.last.sync.date";
    public static final String LOG_NO_PRODUCTS_TO_PROCESS = "kardex.log.no.products.to.process";
    public static final String LOG_PRODUCTS_SAVED = "kardex.log.products.saved";
    public static final String LOG_PROCESSING_PRODUCTS_ERROR = "kardex.log.processing.products.error";
    public static final String LOG_SYNC_STATE_CREATED = "kardex.log.sync.state.created";
    
    // Errores de sincronización
    public static final String ERROR_SYNC_PRODUCTS = "kardex.error.sync.products";
    
    // Mensajes de log para consultas
    public static final String LOG_KARDEX_QUERY_STARTED = "kardex.log.kardex.query.started";
    public static final String LOG_KARDEX_QUERY_WITH_DATES = "kardex.log.kardex.query.with.dates";
    public static final String LOG_PRODUCT_QUERY_ALL = "kardex.log.product.query.all";
}
