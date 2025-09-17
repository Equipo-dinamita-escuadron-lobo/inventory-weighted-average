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

    @Override
    public Kardex registerPurchase(Kardex kardex) {
        log.info(messageService.getMessage(MessageKeys.LOG_PURCHASE_STARTED, 
            kardex.getProductId(), kardex.getFactCode(), kardex.getQuantity()));
            
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
                messageService.getMessage(MessageKeys.ERROR_BALANCE_UNIT_PRICE_ZERO));
        }

        Stock stock = stockIntegrationService.createStock(kardex);
        stockIntegrationService.callApiStockService(stock, true);

        Kardex savedKardex = kardexCommandRepositoryPort.registerPurchase(kardex);
        
        log.info(messageService.getMessage(MessageKeys.LOG_PURCHASE_COMPLETED, 
            savedKardex.getProductId(), savedKardex.getId()));
            
        return savedKardex;
    }

    @Override
    public Kardex registerSale(Kardex kardex) {
        log.info(messageService.getMessage(MessageKeys.LOG_SALE_STARTED, 
            kardex.getProductId(), kardex.getFactCode(), kardex.getQuantity()));
            
        validationService.validateProductExists(kardex.getProductId());
        validationService.validateBusinessRules(kardex.getFactCode(), kardex.getProductId(), MovementType.SALE);
        
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getProductId());
        validationService.validatePreviousKardexExists(lastRegisteredKardex, "sale");
        
        kardex.setType(MovementType.SALE);
        kardex.addSale(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getBalanceUnitPrice(), lastRegisteredKardex.getTotalBalance());

        Stock stock = stockIntegrationService.createStock(kardex);
        stockIntegrationService.callApiStockService(stock, false);

        Kardex savedKardex = kardexCommandRepositoryPort.registerSale(kardex);
        
        log.info(messageService.getMessage(MessageKeys.LOG_SALE_COMPLETED, 
            savedKardex.getProductId(), savedKardex.getId()));
            
        return savedKardex;
    }


    @Override
    public Kardex registerReturnOnPurchase(Kardex kardex) {
        log.info(messageService.getMessage(MessageKeys.LOG_PURCHASE_RETURN_STARTED, 
            kardex.getProductId(), kardex.getFactCode(), kardex.getQuantity()));
            
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
        
        log.info(messageService.getMessage(MessageKeys.LOG_PURCHASE_RETURN_COMPLETED, 
            savedKardex.getProductId(), savedKardex.getId()));
            
        return savedKardex;
    }

    @Override
    public Kardex registerReturnOnSale(Kardex kardex) {
        log.info(messageService.getMessage(MessageKeys.LOG_SALE_RETURN_STARTED, 
            kardex.getProductId(), kardex.getFactCode(), kardex.getQuantity()));
            
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
        
        log.info(messageService.getMessage(MessageKeys.LOG_SALE_RETURN_COMPLETED, 
            savedKardex.getProductId(), savedKardex.getId()));
            
        return savedKardex;
    }

}
