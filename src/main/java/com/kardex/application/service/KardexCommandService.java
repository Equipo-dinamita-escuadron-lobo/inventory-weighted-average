package com.kardex.application.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kardex.application.ports.input.IKardexCommandPort;
import com.kardex.domain.model.Kardex;
import com.kardex.domain.model.MovementType;
import com.kardex.domain.port.IFormatterResultOutputPort;
import com.kardex.domain.port.IKardexCommandRepositoryPort;
import com.kardex.domain.port.IKardexQueryRepositoryPort;
import com.kardex.domain.port.IProductQueryRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KardexCommandService implements IKardexCommandPort{
    private final IKardexCommandRepositoryPort kardexCommandRepositoryPort;
    private final IKardexQueryRepositoryPort kardexQueryRepositoryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IProductQueryRepositoryPort productQueryRepositoryPort;

    @Override
    public Kardex registerPurchase(Kardex kardex) {
        existsProductById(kardex.getIdProduct());
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getIdProduct());
        if (lastRegisteredKardex == null) {
            kardex.setBalanceQuantity(kardex.getQuantity());
            kardex.setBalanceUnitPrice(kardex.getUnitPrice());
            kardex.setTotalBalance(kardex.getUnitPrice().multiply(BigDecimal.valueOf(kardex.getQuantity())));
        } else {
            kardex.addPurchase(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getTotalBalance());
        }
        
        if(kardex.getBalanceUnitPrice() == BigDecimal.ZERO) {
            formatterResultOutputPort.returnResponseError(400, "The balance unit price must be greater than zero.");
        }
        kardex.setType(MovementType.PURCHASE);
        kardex.addDate();
        return kardexCommandRepositoryPort.registerPurchase(kardex);
    }

    @Override
    public Kardex registerSale(Kardex kardex) {
        existsProductById(kardex.getIdProduct());
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getIdProduct());
        if (lastRegisteredKardex == null) {
            formatterResultOutputPort.returnResponseError(400, "No previous kardex found for the product.");
        }
        kardex.addSale(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getBalanceUnitPrice(), lastRegisteredKardex.getTotalBalance());
        kardex.setType(MovementType.SALE);
        kardex.addDate();
        return kardexCommandRepositoryPort.registerSale(kardex);
    }


    @Override
    public Kardex registerReturnOnPurchase(Kardex kardex) {
        existsProductById(kardex.getIdProduct());
        checkReturn(kardex.getFactCode(), kardex.getQuantity());
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getIdProduct());
      
        if (lastRegisteredKardex == null) {
            formatterResultOutputPort.returnResponseError(400, "No previous kardex found for the product.");
        }
        kardex.returnOnPurchase(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getTotalBalance());
        kardex.setType(MovementType.PURCHASERETURN);
        kardex.addDate();
        return kardexCommandRepositoryPort.registerReturnOnPurchase(kardex);
    }

    @Override
    public Kardex registerReturnOnSale(Kardex kardex) {
        existsProductById(kardex.getIdProduct());
        checkReturn(kardex.getFactCode(), kardex.getQuantity());
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getIdProduct());
        if (lastRegisteredKardex == null) {
            formatterResultOutputPort.returnResponseError(400, "No previous kardex found for the product.");
        }
        kardex.returnOnSale(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getBalanceUnitPrice(), lastRegisteredKardex.getTotalBalance());
        kardex.setType(MovementType.SALESRETURN);
        kardex.addDate();
        return kardexCommandRepositoryPort.registerReturnOnSale(kardex);
    }

    private void checkReturn(Long factCode, Long quantity) {
        List<Kardex> kardexList = kardexQueryRepositoryPort.findByFactCode(factCode);
        if (kardexList.isEmpty()) {
            formatterResultOutputPort.returnResponseError(400, "No kardex found for the provided fact code.");
        }

        Long initialInvoceQuantity = kardexList.get(0).getQuantity();

        // The sum of the rest of the list cannot exceed this quantity
        Long totalReturnQuantity = kardexList.stream().skip(1).mapToLong(Kardex::getQuantity).sum() + quantity;
        if (totalReturnQuantity < initialInvoceQuantity) {         
            return;
        }
        formatterResultOutputPort.returnResponseError(400, "The quantity refunded exceeds the original quantity on the invoice.");
    }

    private void existsProductById(Long productId) {
        if (!productQueryRepositoryPort.existsByIdProduct(productId)) {
            formatterResultOutputPort.returnResponseError(404, "Product not found.");
        }
    }
}
