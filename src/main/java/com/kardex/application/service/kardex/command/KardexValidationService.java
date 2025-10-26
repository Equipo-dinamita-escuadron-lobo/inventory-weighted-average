package com.kardex.application.service.kardex.command;

import org.springframework.stereotype.Service;

import com.kardex.domain.model.Kardex;
import com.kardex.domain.model.MovementType;
import com.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;
import com.kardex.infrastructure.adapters.output.jpa.mapper.ProductMapper;
import com.kardex.infrastructure.adapters.output.jpa.repository.IProductRepository;
import com.kardex.domain.model.Product;
import com.kardex.infrastructure.adapters.output.exception.customized.BusinessRuleException;
import com.kardex.domain.port.common.IMessageServicePort;
import com.kardex.domain.port.kardex.IKardexQueryRepositoryPort;
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
    private final IProductRepository productRepository;
    private final ProductMapper productMapper;
    private final IMessageServicePort messageService;

    /**
     * @brief Validates business rules and returns the product
     * @param factCode Invoice/document code
     * @param productId Product identifier
     * @param movementType Type of inventory movement
     * @return Product The validated product with enterprise information
     */
    public Product validateBusinessRulesAndGetProduct(String factCode, Long productId, MovementType movementType) { 
        // Check for duplicate movements based on factCode, productId, and type
        boolean exists = kardexQueryRepositoryPort.existsByFactCodeAndProductIdAndType(factCode, productId, movementType);
        
        if (exists) {
            String movementDescription = movementType.getDescription();
            String errorMessage = messageService.getMessage(MessageKeys.ERROR_DUPLICATE_RECORD, 
                "factCode=" + factCode + ", productId=" + productId + ", type=" + movementDescription);
            throw new BusinessRuleException(400, errorMessage);
        }

        // Get and validate product exists
        ProductEntity productEntity = productRepository.getReferenceByProductId(productId);
        if (productEntity == null) {
            String errorMessage = messageService.getMessage(MessageKeys.ERROR_NOT_FOUND, productId, "Product");
            throw new BusinessRuleException(404, errorMessage);
        }

        Product product = productMapper.toDomain(productEntity);
        if (!product.isActive()) {
            String errorMessage = messageService.getMessage(MessageKeys.ERROR_OPERATION_NOT_ALLOWED, 
                "Product is not active: " + productId);
            throw new BusinessRuleException(400, errorMessage);
        }

        return product;
    }

    /**
     * @brief Validates that previous kardex exists for dependent operations
     * @param lastRegisteredKardex Last kardex record for the product
     * @param operation Operation name for error messaging
     */
    public void validatePreviousKardexExists(Kardex lastRegisteredKardex, String operation) {
        if (lastRegisteredKardex == null) {
            String errorMessage = messageService.getMessage(MessageKeys.ERROR_MISSING_RECORD, operation);
            throw new BusinessRuleException(404, errorMessage);
        }
    }
}
