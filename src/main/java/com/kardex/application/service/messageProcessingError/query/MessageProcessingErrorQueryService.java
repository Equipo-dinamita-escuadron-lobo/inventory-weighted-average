package com.kardex.application.service.messageProcessingError.query;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.kardex.application.ports.input.IMessageProcessingErrorQueryPort;
import com.kardex.domain.model.MessageProcessingError;
import com.kardex.domain.port.IMessageProcessingErrorQueryRepositoryPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Service implementation for MessageProcessingError query operations
 * 
 * Provides read access to message processing error records with
 * pagination support and individual record access.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProcessingErrorQueryService implements IMessageProcessingErrorQueryPort {

    private final IMessageProcessingErrorQueryRepositoryPort messageProcessingErrorQueryRepositoryPort;

    /**
     * @brief Finds a message processing error by ID
     * @param id Error record identifier
     * @return Optional containing the error record if found
     */
    @Override
    public Optional<MessageProcessingError> findById(Long id) {
        log.info("Finding message processing error by id: {}", id);
        return messageProcessingErrorQueryRepositoryPort.findById(id);
    }

    /**
     * @brief Retrieves all message processing errors with pagination
     * @param pageable Pagination parameters
     * @return Paginated error records
     */
    @Override
    public Page<MessageProcessingError> findAll(Pageable pageable) {
        log.info("Finding all message processing errors with pagination - page: {}, size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        return messageProcessingErrorQueryRepositoryPort.findAll(pageable);
    }
}