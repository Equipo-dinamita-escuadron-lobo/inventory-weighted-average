package com.kardex.application.service.kardex.command;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.kardex.application.ports.input.IKardexCommandPort;
import com.kardex.domain.model.Kardex;
import com.kardex.domain.model.MovementType;
import com.kardex.domain.model.Stock;
import com.kardex.domain.port.IFormatterResultOutputPort;
import com.kardex.domain.port.IKardexCommandRepositoryPort;
import com.kardex.domain.port.IKardexQueryRepositoryPort;
import com.kardex.domain.port.IMessageServicePort;
import com.kardex.infrastructure.adapters.config.i18n.MessageKeys;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Service implementation for Kardex command operations
 * 
 * Processes inventory movements using weighted average cost method.
 * Handles purchases, sales, and returns with stock integration.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class KardexCommandService implements IKardexCommandPort{
    
    private final IKardexCommandRepositoryPort kardexCommandRepositoryPort;
    private final IKardexQueryRepositoryPort kardexQueryRepositoryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IMessageServicePort messageService;
    
    // Servicios auxiliares
    private final KardexValidationService validationService;
    private final KardexReturnService returnService;
    private final StockIntegrationService stockIntegrationService;

    /**
     * @brief Registers a purchase transaction with weighted average calculation
     * @param kardex Purchase details to process
     * @return Processed kardex with updated inventory balances
     */
    @Override
    public Kardex registerPurchase(Kardex kardex) {  
        validationService.validateProductExists(kardex.getProductId());
        validationService.validateBusinessRules(kardex.getFactCode(), kardex.getProductId(), MovementType.PURCHASE);
        
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getProductId());
        kardex.setType(MovementType.PURCHASE);
        if (lastRegisteredKardex == null) {
            kardex.setBalanceQuantity(kardex.getQuantity());
            kardex.setBalanceUnitPrice(kardex.getUnitPrice());
            kardex.setTotalBalance(kardex.getUnitPrice().multiply(BigDecimal.valueOf(kardex.getQuantity())));
            kardex.finalizeKardexEntry();
        } else {
            kardex.addPurchase(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getTotalBalance());
        }
        
        if(kardex.getBalanceUnitPrice().compareTo(BigDecimal.ZERO) == 0) {
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, 
                messageService.getMessage(MessageKeys.ERROR_INVALID_VALUE, "balance unit price"));
        }

        Stock stock = stockIntegrationService.createStock(kardex);
        stockIntegrationService.callApiStockService(stock, true);

        Kardex savedKardex = kardexCommandRepositoryPort.registerPurchase(kardex);

        log.info("Method registerPurchase productId=" + savedKardex.getProductId());
        return savedKardex;
    }

    /**
     * @brief Registers a sale transaction with inventory validation
     * @param kardex Sale details to process
     * @return Processed kardex with updated inventory balances
     */
    @Override
    public Kardex registerSale(Kardex kardex) {
        validationService.validateProductExists(kardex.getProductId());
        validationService.validateBusinessRules(kardex.getFactCode(), kardex.getProductId(), MovementType.SALE);
        
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getProductId());
        validationService.validatePreviousKardexExists(lastRegisteredKardex, "sale");
        
        kardex.setType(MovementType.SALE);
        kardex.addSale(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getBalanceUnitPrice(), lastRegisteredKardex.getTotalBalance());

        Stock stock = stockIntegrationService.createStock(kardex);
        stockIntegrationService.callApiStockService(stock, false);

        Kardex savedKardex = kardexCommandRepositoryPort.registerSale(kardex);

        log.info("Method registerSale productId=" + savedKardex.getProductId());
        return savedKardex;
    }


    /**
     * @brief Registers a purchase return with original price validation
     * @param kardex Return details to process
     * @return Processed kardex with updated inventory balances
     */
    @Override
    public Kardex registerReturnOnPurchase(Kardex kardex) {        
        validationService.validateProductExists(kardex.getProductId());
        BigDecimal unitPrice = returnService.getUnitPriceIfReturnAllowed(kardex.getFactCode(), kardex.getQuantity(), kardex.getProductId(), MovementType.PURCHASE);
        kardex.setUnitPrice(unitPrice);
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getProductId());
        kardex.setType(MovementType.PURCHASERETURN);

        validationService.validatePreviousKardexExists(lastRegisteredKardex, "purchase return");
        kardex.returnOnPurchase(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getTotalBalance());

        Stock stock = stockIntegrationService.createStock(kardex);
        stockIntegrationService.callApiStockService(stock, false);

        Kardex savedKardex = kardexCommandRepositoryPort.registerReturnOnPurchase(kardex);
        
        log.info("Method registerReturnOnPurchase productId=" + savedKardex.getProductId());
        return savedKardex;
    }

    /**
     * @brief Registers a sale return with original price validation
     * @param kardex Return details to process
     * @return Processed kardex with updated inventory balances
     */
    @Override
    public Kardex registerReturnOnSale(Kardex kardex) {   
        validationService.validateProductExists(kardex.getProductId());
        BigDecimal unitPrice = returnService.getUnitPriceIfReturnAllowed(kardex.getFactCode(), kardex.getQuantity(), kardex.getProductId(), MovementType.SALE);
        kardex.setUnitPrice(unitPrice);
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getProductId());
        kardex.setType(MovementType.SALESRETURN);

        validationService.validatePreviousKardexExists(lastRegisteredKardex, "sale return");

        kardex.returnOnSale(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getBalanceUnitPrice(), lastRegisteredKardex.getTotalBalance());

        Stock stock = stockIntegrationService.createStock(kardex);
        stockIntegrationService.callApiStockService(stock, true);

        Kardex savedKardex = kardexCommandRepositoryPort.registerReturnOnSale(kardex);
        
        log.info(" Method registerReturnOnSale productId=" + savedKardex.getProductId());
        return savedKardex;
    }

    /**
     * @brief Deletes all kardex records
     */
    @Override
    public void deleteAll() {
        log.info("Deleting all kardex records");
        try {
            kardexCommandRepositoryPort.deleteAll();
            log.info("All kardex records deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting all kardex records: {}", e.getMessage());
        }
    }

}
