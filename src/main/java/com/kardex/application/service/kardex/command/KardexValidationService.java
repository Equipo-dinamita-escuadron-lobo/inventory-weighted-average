package com.kardex.application.service.kardex.command;

import org.springframework.stereotype.Service;

import com.kardex.domain.model.Kardex;
import com.kardex.domain.model.MovementType;
import com.kardex.domain.port.IFormatterResultOutputPort;
import com.kardex.domain.port.IKardexQueryRepositoryPort;
import com.kardex.domain.port.IMessageServicePort;
import com.kardex.domain.port.IProductQueryRepositoryPort;
import com.kardex.infrastructure.adapters.config.i18n.MessageKeys;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Service for validating Kardex business rules and data integrity
 * 
 * Ensures business constraints are met before processing inventory movements,
 * including duplicate prevention and prerequisite validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KardexValidationService {
    
    private final IKardexQueryRepositoryPort kardexQueryRepositoryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IProductQueryRepositoryPort productQueryRepositoryPort;
    private final IMessageServicePort messageService;

    /**
     * @brief Validates business rules for a kardex movement
     * @param factCode Invoice/document code
     * @param productId Product identifier
     * @param movementType Type of inventory movement
     */
    public void validateBusinessRules(String factCode, Long productId, MovementType movementType) {
        log.debug(messageService.getMessage(MessageKeys.LOG_VALIDATING_BUSINESS_RULES, 
            factCode, productId, movementType.getDescription()));
            
        // Check for duplicate movements based on factCode, productId, and type
        boolean exists = kardexQueryRepositoryPort.existsByFactCodeAndProductIdAndType(factCode, productId, movementType);
        
        if (exists) {
            String movementDescription = movementType.getDescription();
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, 
                messageService.getMessage(MessageKeys.ERROR_DUPLICATE_MOVEMENT, 
                    movementDescription, factCode, productId));
        }
    }

    /**
     * @brief Validates that previous kardex exists for dependent operations
     * @param lastRegisteredKardex Last kardex record for the product
     * @param operation Operation name for error messaging
     */
    public void validatePreviousKardexExists(Kardex lastRegisteredKardex, String operation) {
        if (lastRegisteredKardex == null) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, 
                messageService.getMessage(MessageKeys.ERROR_NO_PREVIOUS_KARDEX, operation));
        }
    }

    /**
     * @brief Validates that a product exists in the system
     * @param productId Product identifier to validate
     */
    public void validateProductExists(Long productId) {
        log.debug(messageService.getMessage(MessageKeys.LOG_PRODUCT_EXISTS_VALIDATION, productId));
        
        if (!productQueryRepositoryPort.existsByProductId(productId)) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, 
                messageService.getMessage(MessageKeys.ERROR_PRODUCT_NOT_FOUND));
        }
    }
}
