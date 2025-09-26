package com.kardex.application.ports.input;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kardex.domain.model.MessageProcessingError;

/**
 * @brief Input port for MessageProcessingError query operations
 * 
 * Provides query capabilities for retrieving message processing error records
 * with pagination support and individual record access.
 */
public interface IMessageProcessingErrorQueryPort {
    
    /**
     * @brief Finds a message processing error by ID
     * @param id Error record identifier
     * @return Optional containing the error record if found
     */
    Optional<MessageProcessingError> findById(Long id);
    
    /**
     * @brief Retrieves all message processing errors with pagination
     * @param pageable Pagination parameters
     * @return Paginated error records
     */
    Page<MessageProcessingError> findAll(Pageable pageable);
}