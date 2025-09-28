package com.kardex.application.service.messageProcessingError.command;

import org.springframework.stereotype.Service;

import com.kardex.application.ports.input.messageProcessingError.IMessageProcessingErrorCommandPort;
import com.kardex.domain.port.common.IFormatterResultOutputPort;
import com.kardex.domain.port.common.IMessageServicePort;
import com.kardex.domain.port.messageProcessingError.IMessageProcessingErrorCommandRepositoryPort;
import com.kardex.domain.port.messageProcessingError.IMessageProcessingErrorQueryRepositoryPort;
import com.kardex.infrastructure.adapters.config.i18n.MessageKeys;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Service implementation for MessageProcessingError command operations
 * 
 * Handles message processing error deletion operations with validation
 * and transaction management.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MessageProcessingErrorCommandService implements IMessageProcessingErrorCommandPort {
    
    private final IMessageProcessingErrorCommandRepositoryPort messageProcessingErrorCommandRepositoryPort;
    private final IMessageProcessingErrorQueryRepositoryPort messageProcessingErrorQueryRepositoryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IMessageServicePort messageService;

    /**
     * @brief Deletes all message processing error records
     */
    @Override
    public void deleteAll() {
        log.info("Deleting all message processing errors");
        messageProcessingErrorCommandRepositoryPort.deleteAll();
        log.info("All message processing errors deleted successfully");
    }

    /**
     * @brief Deletes a specific message processing error by ID
     * @param id Error record identifier
     */
    @Override
    public void deleteById(Long id) {
        log.info("Deleting message processing error by id: {}", id);
        
        // Validate existence
        if (!messageProcessingErrorQueryRepositoryPort.findById(id).isPresent()) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, 
                messageService.getMessage(MessageKeys.ERROR_NOT_FOUND, "Message processing error with id: " + id));
        }
        
        messageProcessingErrorCommandRepositoryPort.deleteById(id);
        log.info("Message processing error with id {} deleted successfully", id);
    }
}