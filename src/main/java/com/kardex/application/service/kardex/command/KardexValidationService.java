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

@Service
@RequiredArgsConstructor
@Slf4j
public class KardexValidationService {
    
    private final IKardexQueryRepositoryPort kardexQueryRepositoryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IProductQueryRepositoryPort productQueryRepositoryPort;
    private final IMessageServicePort messageService;

    /**
     * Valida las reglas de negocio para un movimiento de kardex
     */
    public void validateBusinessRules(String factCode, Long productId, MovementType movementType) {
        log.debug(messageService.getMessage(MessageKeys.LOG_VALIDATING_BUSINESS_RULES, 
            factCode, productId, movementType.getDescription()));
            
        // Verificar si ya existe el mismo factCode, productId y tipo de movimiento
        boolean exists = kardexQueryRepositoryPort.existsByFactCodeAndProductIdAndType(factCode, productId, movementType);
        
        if (exists) {
            String movementDescription = movementType.getDescription();
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, 
                messageService.getMessage(MessageKeys.ERROR_DUPLICATE_MOVEMENT, 
                    movementDescription, factCode, productId));
        }
    }

    /**
     * Valida que el Kardex anterior exista para operaciones que lo requieren
     */
    public void validatePreviousKardexExists(Kardex lastRegisteredKardex, String operation) {
        if (lastRegisteredKardex == null) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, 
                messageService.getMessage(MessageKeys.ERROR_NO_PREVIOUS_KARDEX, operation));
        }
    }

    /**
     * Valida que el producto exista por su ID
     */
    public void validateProductExists(Long productId) {
        log.debug(messageService.getMessage(MessageKeys.LOG_PRODUCT_EXISTS_VALIDATION, productId));
        
        if (!productQueryRepositoryPort.existsByProductId(productId)) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, 
                messageService.getMessage(MessageKeys.ERROR_PRODUCT_NOT_FOUND));
        }
    }
}
