package com.kardex.application.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.kardex.application.ports.input.IKardexCommandPort;
import com.kardex.application.ports.input.IStockClient;
import com.kardex.domain.model.Kardex;
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'registerPurchase'");
    }

    @Override
    public Kardex registerSale(Kardex kardex) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'registerSale'");
    }
}
