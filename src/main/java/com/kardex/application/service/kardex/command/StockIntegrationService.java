package com.kardex.application.service.kardex.command;

import org.springframework.stereotype.Service;

import com.kardex.domain.model.Kardex;
import com.kardex.domain.model.Stock;
import com.kardex.domain.port.IMessageServicePort;
import com.kardex.domain.port.IStockClientPort;
import com.kardex.infrastructure.adapters.config.i18n.MessageKeys;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Service for integrating inventory movements with external stock system
 * 
 * Handles communication with stock service to maintain real-time
 * inventory synchronization across systems.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockIntegrationService {
    
    private final IStockClientPort stockClient;
    private final IMessageServicePort messageService;

    /**
     * @brief Creates a Stock object from Kardex data
     * @param kardex Source kardex record
     * @return Stock object for external API call
     */
    public Stock createStock(Kardex kardex) {
        return Stock.builder()
            .productId(kardex.getProductId())
            .quantity(kardex.getQuantity())
            .price(kardex.getBalanceUnitPrice())
            .build();
    }

    /**
     * @brief Calls stock service API to update inventory
     * @param stock Stock data to send
     * @param isBuy True for purchase/return operations, false for sales
     */
    public void callApiStockService(Stock stock, boolean isBuy) {
        try {
            if (isBuy) {
                stockClient.buyStock(stock);
            } else {
                stockClient.sellStock(stock);
            }
            log.info(messageService.getMessage(MessageKeys.LOG_OPERATION_COMPLETED, 
                isBuy ? "purchase" : "sale"));
        } catch (Exception e) {
            log.error(messageService.getMessage(MessageKeys.LOG_OPERATION_ERROR, 
                isBuy ? "purchase" : "sale", e.getMessage(), e));
        }
    }
}
