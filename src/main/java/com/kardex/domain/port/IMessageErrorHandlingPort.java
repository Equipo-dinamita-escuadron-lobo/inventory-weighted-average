package com.kardex.domain.port;

/**
 * Puerto para el manejo de errores de procesamiento de mensajes.
 */
public interface IMessageErrorHandlingPort {
    
    /**
     * Guarda información de error cuando falla el procesamiento de un mensaje.
     * 
     * @param eventType Tipo de evento que falló (puede ser null)
     * @param errorDescription Descripción del error ocurrido
     * @param messageData Datos del mensaje en formato JSON
     * @param entityType Tipo de entidad que se estaba procesando
     */
    void saveProcessingError(String eventType, String errorDescription, String messageData, String entityType);
}
