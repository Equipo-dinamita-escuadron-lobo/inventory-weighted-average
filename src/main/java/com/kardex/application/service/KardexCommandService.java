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

    @Override
    public Kardex registerPurchase(Kardex kardex) {
        existsProductById(kardex.getProductId());
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
        
        if(kardex.getBalanceUnitPrice() == BigDecimal.ZERO) {
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, "The balance unit price must be greater than zero.");
        }

        Stock stock= Stock.builder()
            .productId(kardex.getProductId())
            .quantity(kardex.getQuantity())
            .price(kardex.getUnitPrice())
            .build();

        callApiStockService(stock, true);

        return kardexCommandRepositoryPort.registerPurchase(kardex);
    }

    @Override
    public Kardex registerSale(Kardex kardex) {
        existsProductById(kardex.getProductId());
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getProductId());
        if (lastRegisteredKardex == null) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, "No previous kardex found for the product.");
        }
        kardex.setType(MovementType.SALE);
        kardex.addSale(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getBalanceUnitPrice(), lastRegisteredKardex.getTotalBalance());

        Stock stock = Stock.builder()
            .productId(kardex.getProductId())
            .quantity(kardex.getQuantity())
            .price(kardex.getUnitPrice())
            .build();

        callApiStockService(stock, false);

        return kardexCommandRepositoryPort.registerSale(kardex);
    }


    @Override
    public Kardex registerReturnOnPurchase(Kardex kardex) {
        existsProductById(kardex.getProductId());
        BigDecimal unitPrice = getUnitPriceIfReturnAllowed(kardex.getFactCode(), kardex.getQuantity(), kardex.getProductId());
        kardex.setUnitPrice(unitPrice);
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getProductId());
        kardex.setType(MovementType.PURCHASERETURN);

        if (lastRegisteredKardex == null) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, "No previous kardex found for the product.");
        }
        kardex.returnOnPurchase(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getTotalBalance());

        Stock stock = Stock.builder()
            .productId(kardex.getProductId())
            .quantity(kardex.getQuantity())
            .price(kardex.getUnitPrice())
            .build();

        callApiStockService(stock, false);

        return kardexCommandRepositoryPort.registerReturnOnPurchase(kardex);
    }

    @Override
    public Kardex registerReturnOnSale(Kardex kardex) {
        existsProductById(kardex.getProductId());
        BigDecimal unitPrice = getUnitPriceIfReturnAllowed(kardex.getFactCode(), kardex.getQuantity(), kardex.getProductId());
        kardex.setUnitPrice(unitPrice);
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getProductId());
        kardex.setType(MovementType.SALESRETURN);

        if (lastRegisteredKardex == null) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, "No previous kardex found for the product.");
        }

        kardex.returnOnSale(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getBalanceUnitPrice(), lastRegisteredKardex.getTotalBalance());

        Stock stock = Stock.builder()
            .productId(kardex.getProductId())
            .quantity(kardex.getQuantity())
            .price(kardex.getUnitPrice())
            .build();

        callApiStockService(stock, true);

        return kardexCommandRepositoryPort.registerReturnOnSale(kardex);
    }

    private BigDecimal getUnitPriceIfReturnAllowed(Long factCode, int quantity, Long productId) {
        List<Kardex> kardexList = kardexQueryRepositoryPort.findByFactCodeAndProductId(factCode, productId);
        if (kardexList.isEmpty()) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, "No kardex found for the provided fact code.");
        }

        int initialInvoceQuantity = kardexList.get(0).getQuantity();
        BigDecimal unitPrice = kardexList.get(0).getUnitPrice();

        // The sum of the rest of the list cannot exceed this quantity
        int totalReturnQuantity = kardexList.stream().skip(1).mapToInt(Kardex::getQuantity).sum() + quantity;
        if (totalReturnQuantity < initialInvoceQuantity) {
            return unitPrice;
        }
        formatterResultOutputPort.returnBusinessRuleErrorResponse(400, "The quantity refunded exceeds the original quantity on the invoice.");
        return BigDecimal.ZERO;
    }

    private void existsProductById(Long productId) {
        if (!productQueryRepositoryPort.existsByProductId(productId)) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, "Product not found.");
        }
    }

    private void callApiStockService(Stock stock, boolean isBuy) {
        try {
            if (isBuy) {
                stockClient.buyStock(stock);
            } else {
                stockClient.sellStock(stock);
            }
            log.info("Stock {} request sent successfully.", isBuy ? "purchase" : "sale");
        } catch (Exception e) {
            log.error("Error sending stock {} request: {}", isBuy ? "purchase" : "sale", e.getMessage());
        }
    }
}
