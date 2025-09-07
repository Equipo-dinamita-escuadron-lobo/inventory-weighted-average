package com.kardex.application.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kardex.application.ports.input.IKardexCommandPort;
import com.kardex.domain.model.Kardex;
import com.kardex.domain.model.MovementType;
import com.kardex.domain.model.Stock;
import com.kardex.domain.port.IFormatterResultOutputPort;
import com.kardex.domain.port.IKardexCommandRepositoryPort;
import com.kardex.domain.port.IKardexQueryRepositoryPort;
import com.kardex.domain.port.IProductQueryRepositoryPort;
import com.kardex.domain.port.IStockClientPort;
import com.kardex.infrastructure.adapters.config.i18n.MessageKeys;
import com.kardex.infrastructure.adapters.config.i18n.MessageService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class KardexCommandService implements IKardexCommandPort{
    
    private final IKardexCommandRepositoryPort kardexCommandRepositoryPort;
    private final IKardexQueryRepositoryPort kardexQueryRepositoryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IProductQueryRepositoryPort productQueryRepositoryPort;
    private final IStockClientPort stockClient;
    private final MessageService messageService;

    @Override
    public Kardex registerPurchase(Kardex kardex) {
        existsProductById(kardex.getProductId());
        validateBusinessRules(kardex.getFactCode(), kardex.getProductId(), MovementType.PURCHASE);
        
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getProductId());
        kardex.setType(MovementType.PURCHASE);
        if (lastRegisteredKardex == null) {
            kardex.setBalanceQuantity(kardex.getQuantity());
            kardex.setBalanceUnitPrice(kardex.getUnitPrice());
            kardex.setTotalBalance(kardex.getUnitPrice().multiply(BigDecimal.valueOf(kardex.getQuantity())));
            kardex.addDate();
            kardex.updateDetailIfNotNull();
        } else {
            kardex.addPurchase(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getTotalBalance());
        }
        
        if(kardex.getBalanceUnitPrice().compareTo(BigDecimal.ZERO) == 0) {
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, 
                messageService.getMessage(MessageKeys.ERROR_BALANCE_UNIT_PRICE_ZERO));
        }

        Stock stock = createStock(kardex);
        callApiStockService(stock, true);

        return kardexCommandRepositoryPort.registerPurchase(kardex);
    }

    @Override
    public Kardex registerSale(Kardex kardex) {
        existsProductById(kardex.getProductId());
        validateBusinessRules(kardex.getFactCode(), kardex.getProductId(), MovementType.SALE);
        
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getProductId());
        validatePreviousKardexExists(lastRegisteredKardex, "sale");
        
        kardex.setType(MovementType.SALE);
        kardex.addSale(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getBalanceUnitPrice(), lastRegisteredKardex.getTotalBalance());

        Stock stock = createStock(kardex);
        callApiStockService(stock, false);

        return kardexCommandRepositoryPort.registerSale(kardex);
    }


    @Override
    public Kardex registerReturnOnPurchase(Kardex kardex) {
        existsProductById(kardex.getProductId());
        BigDecimal unitPrice = getUnitPriceIfReturnAllowed(kardex.getFactCode(), kardex.getQuantity(), kardex.getProductId(), MovementType.PURCHASE);
        kardex.setUnitPrice(unitPrice);
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getProductId());
        kardex.setType(MovementType.PURCHASERETURN);

        validatePreviousKardexExists(lastRegisteredKardex, "purchase return");
        kardex.returnOnPurchase(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getTotalBalance());

        Stock stock = createStock(kardex);
        callApiStockService(stock, false);

        return kardexCommandRepositoryPort.registerReturnOnPurchase(kardex);
    }

    @Override
    public Kardex registerReturnOnSale(Kardex kardex) {
        existsProductById(kardex.getProductId());
        BigDecimal unitPrice = getUnitPriceIfReturnAllowed(kardex.getFactCode(), kardex.getQuantity(), kardex.getProductId(), MovementType.SALE);
        kardex.setUnitPrice(unitPrice);
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getProductId());
        kardex.setType(MovementType.SALESRETURN);

        validatePreviousKardexExists(lastRegisteredKardex, "sale return");

        kardex.returnOnSale(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getBalanceUnitPrice(), lastRegisteredKardex.getTotalBalance());

        Stock stock = createStock(kardex);
        callApiStockService(stock, true);

        return kardexCommandRepositoryPort.registerReturnOnSale(kardex);
    }

    /**
     * Crea un objeto Stock basado en los datos del Kardex
     */
    private Stock createStock(Kardex kardex) {
        return Stock.builder()
            .productId(kardex.getProductId())
            .quantity(kardex.getQuantity())
            .price(kardex.getBalanceUnitPrice())
            .build();
    }

    /**
     * Valida que el Kardex anterior exista para operaciones que lo requieren
     */
    private void validatePreviousKardexExists(Kardex lastRegisteredKardex, String operation) {
        if (lastRegisteredKardex == null) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, 
                messageService.getMessage(MessageKeys.ERROR_NO_PREVIOUS_KARDEX, operation));
        }
    }

    private BigDecimal getUnitPriceIfReturnAllowed(Long factCode, int quantity, Long productId, MovementType originalMovementType) {
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

    private MovementType getReturnType(MovementType originalType) {
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

    private void existsProductById(Long productId) {
        if (!productQueryRepositoryPort.existsByProductId(productId)) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, 
                messageService.getMessage(MessageKeys.ERROR_PRODUCT_NOT_FOUND));
        }
    }

    private void callApiStockService(Stock stock, boolean isBuy) {
        try {
            if (isBuy) {
                stockClient.buyStock(stock);
            } else {
                stockClient.sellStock(stock);
            }
            log.info(messageService.getMessage(MessageKeys.LOG_STOCK_REQUEST_SUCCESS, 
                isBuy ? "purchase" : "sale"));
        } catch (Exception e) {
            log.error(messageService.getMessage(MessageKeys.LOG_STOCK_REQUEST_ERROR, 
                isBuy ? "purchase" : "sale", e.getMessage()));
        }
    }

    private void validateBusinessRules(Long factCode, Long productId, MovementType movementType) {
        // Verificar si ya existe el mismo factCode, productId y tipo de movimiento
        boolean exists = kardexQueryRepositoryPort.existsByFactCodeAndProductIdAndType(factCode, productId, movementType);
        
        if (exists) {
            String movementDescription = movementType.getDescription();
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, 
                messageService.getMessage(MessageKeys.ERROR_DUPLICATE_MOVEMENT, 
                    movementDescription, factCode, productId));
        }
    }
}
