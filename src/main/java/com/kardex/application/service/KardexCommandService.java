package com.kardex.application.service;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.kardex.application.ports.input.IKardexCommandPort;
import com.kardex.application.ports.input.IStockClient;
import com.kardex.domain.model.Kardex;
import com.kardex.domain.port.IFormatterResultOutputPort;
import com.kardex.domain.port.IKardexCommandRepositoryPort;
import com.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import com.kardex.infrastructure.adapters.input.rest.dto.request.StockDtoRequest;
import com.kardex.infrastructure.adapters.input.rest.dto.response.StockDtoResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KardexCommandService implements IKardexCommandPort{
    private final IStockClient stockClient;
    private final IKardexCommandRepositoryPort kardexCommandRepositoryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;

    @Override
    public void test(String message) {
        StockDtoRequest stockDtoRequest = new StockDtoRequest(); 
        stockDtoRequest.setProductId(1L);
        stockDtoRequest.setQuantity(10);
        stockDtoRequest.setPrice(100.0);

        try {
            ResponseEntity<ResponseDto<StockDtoResponse>> response = stockClient.buyStock(stockDtoRequest);

            if (response.getStatusCode().is2xxSuccessful()) {
                ResponseDto<StockDtoResponse> responseBody = response.getBody();
                if (responseBody != null && responseBody.getData() != null) {
                    StockDtoResponse stockResponse = responseBody.getData();
                    System.out.println("Stock purchased successfully ID: " + stockResponse.getProductId() 
                    + " - Quantity: " + stockResponse.getQuantity() 
                    + " - Price: " + stockResponse.getPrice());
                } else {
                    System.out.println("No data found in the response.");
                }         
            } else {
                System.out.println("Error purchasing stock: " + response.getBody().getMessage());
            }

        } catch (Exception e) {
            System.out.println("An error occurred while purchasing stock: " + e.getMessage());
        }
    }

    @Override
    public Kardex registerPurchase(Kardex kardex) {
        Kardex lastRegisteredKardex = kardexCommandRepositoryPort.getLatestKardexByProductId(kardex.getProduct().getId());
        if (lastRegisteredKardex == null) {
            kardex.setBalanceQuantity(kardex.getQuantity());
            kardex.setBalanceUnitPrice(kardex.getUnitPrice());
        } else {
            kardex.addPurchaseBalance(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getBalanceUnitPrice());
        }
        
        if(kardex.getBalanceUnitPrice() == BigDecimal.ZERO) {
            formatterResultOutputPort.returnResponseError(400, "The balance unit price must be greater than zero.");
        }
        kardex.addDate();
        return kardexCommandRepositoryPort.registerPurchase(kardex);
    }

    @Override
    public Kardex registerSale(Kardex kardex) {
        Kardex lastRegisteredKardex = kardexCommandRepositoryPort.getLatestKardexByProductId(kardex.getProduct().getId());
        if (lastRegisteredKardex == null) {
            formatterResultOutputPort.returnResponseError(400, "No previous kardex found for the product.");
        }
        kardex.addSaleBalance(lastRegisteredKardex.getQuantity(), lastRegisteredKardex.getUnitPrice());
        kardex.addDate();
        return kardexCommandRepositoryPort.registerSale(kardex);
    }
}
