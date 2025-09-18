package com.kardex.application.service.kardex.command;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kardex.domain.model.Kardex;
import com.kardex.domain.model.MovementType;
import com.kardex.domain.port.IFormatterResultOutputPort;
import com.kardex.domain.port.IKardexQueryRepositoryPort;
import com.kardex.domain.port.IMessageServicePort;
import com.kardex.infrastructure.adapters.config.i18n.MessageKeys;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KardexReturnService {
    
    private final IKardexQueryRepositoryPort kardexQueryRepositoryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IMessageServicePort messageService;

    /**
     * Obtiene el precio unitario si la devolución está permitida
     */
    public BigDecimal getUnitPriceIfReturnAllowed(String factCode, int quantity, Long productId, MovementType originalMovementType) {
        // Buscar específicamente por el tipo de movimiento original
        List<Kardex> originalKardexList = kardexQueryRepositoryPort.findByFactCodeAndProductIdAndType(factCode, productId, originalMovementType);
        if (originalKardexList.isEmpty()) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, 
                messageService.getMessage(MessageKeys.ERROR_NO_ORIGINAL_MOVEMENT, originalMovementType.getDescription()));
        }

        // El primer registro debe ser la operación original
        Kardex originalKardex = originalKardexList.get(0);
        int initialInvoiceQuantity = originalKardex.getQuantity();
        BigDecimal unitPrice = originalKardex.getUnitPrice();

        // Ahora buscar todas las devoluciones previas de este mismo factCode y productId
        MovementType returnType = getReturnType(originalMovementType);
        List<Kardex> returnKardexList = kardexQueryRepositoryPort.findByFactCodeAndProductIdAndType(factCode, productId, returnType);
        
        // Calcular el total de devoluciones previas más la cantidad actual
        int totalReturnQuantity = returnKardexList.stream().mapToInt(Kardex::getQuantity).sum() + quantity;
        
        if (totalReturnQuantity <= initialInvoiceQuantity) {
            return unitPrice;
        }
        formatterResultOutputPort.returnBusinessRuleErrorResponse(400, 
            messageService.getMessage(MessageKeys.ERROR_RETURN_QUANTITY_EXCEEDED));
        return BigDecimal.ZERO;
    }

    /**
     * Obtiene el tipo de movimiento de devolución correspondiente al tipo original
     */
    public MovementType getReturnType(MovementType originalType) {
        switch (originalType) {
            case PURCHASE:
                return MovementType.PURCHASERETURN;
            case SALE:
                return MovementType.SALESRETURN;
            default:
                throw new IllegalArgumentException(
                    messageService.getMessage(MessageKeys.ERROR_INVALID_MOVEMENT_TYPE, originalType));
        }
    }
}
