package com.kardex.infrastructure.adapters.output.jpa.adapter;

import org.springframework.stereotype.Repository;

import com.kardex.domain.port.IMessageErrorHandlingPort;
import com.kardex.infrastructure.adapters.output.jpa.entity.MessageProcessingErrorEntity;
import com.kardex.infrastructure.adapters.output.jpa.repository.IMessageProcessingErrorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador para el manejo de errores de procesamiento de mensajes.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class MessageErrorHandlingAdapter implements IMessageErrorHandlingPort {

    private final IMessageProcessingErrorRepository errorRepository;

    @Override
    public void saveProcessingError(String eventType, String errorDescription, String messageData, String entityType) {
        try {
            MessageProcessingErrorEntity errorEntity = new MessageProcessingErrorEntity();
            errorEntity.setEventType(eventType != null ? eventType : "null_event_type");
            errorEntity.setErrorDescription(errorDescription);
            errorEntity.setMessageData(messageData);
            errorEntity.setEntityType(entityType);
            
            errorRepository.save(errorEntity);
            log.info("Processing error saved for entity type: {}, event type: {}", entityType, eventType);
            
        } catch (Exception e) {
            log.error("Failed to save processing error to database: {}", e.getMessage(), e);
        }
    }
}
