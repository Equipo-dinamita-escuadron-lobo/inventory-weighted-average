package com.kardex.domain.port;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kardex.domain.model.MessageProcessingError;

/**
 * @brief Output port for MessageProcessingError query operations
 * 
 * Defines the contract for accessing message processing error data
 * from the persistence layer.
 */
public interface IMessageProcessingErrorQueryRepositoryPort {
    
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