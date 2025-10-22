package com.kardex.application.service.kardex.command;

import org.springframework.stereotype.Service;

import com.kardex.domain.model.Kardex;
import com.kardex.domain.model.MovementType;
import com.kardex.domain.port.common.IFormatterResultOutputPort;
import com.kardex.domain.port.common.IMessageServicePort;
import com.kardex.domain.port.kardex.IKardexQueryRepositoryPort;
import com.kardex.domain.port.product.IProductQueryRepositoryPort;
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
        // Check for duplicate movements based on factCode, productId, and type
        boolean exists = kardexQueryRepositoryPort.existsByFactCodeAndProductIdAndType(factCode, productId, movementType);
        
        if (exists) {
            String movementDescription = movementType.getDescription();
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, 
                messageService.getMessage(MessageKeys.ERROR_DUPLICATE_RECORD, "factCode=" + factCode + ", productId=" + productId + ", type=" + movementDescription));
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
                messageService.getMessage(MessageKeys.ERROR_MISSING_RECORD, operation));
        }
    }

    /**
     * @brief Validates that a product exists in the system
     * @param productId Product identifier to validate
     */
    public void validateProductExists(Long productId) {       
        if (!productQueryRepositoryPort.existsByProductId(productId)) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, 
                messageService.getMessage(MessageKeys.ERROR_NOT_FOUND, productId, "Product"));
        }
    }
}
