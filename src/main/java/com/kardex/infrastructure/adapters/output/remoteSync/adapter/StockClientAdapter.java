package com.kardex.infrastructure.adapters.output.remoteSync.adapter;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.kardex.domain.model.Stock;
import com.kardex.domain.port.IStockClientPort;
import com.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import com.kardex.infrastructure.adapters.output.remoteSync.config.IStockClient;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.StockDtoResponse;
import com.kardex.infrastructure.adapters.output.remoteSync.mapper.IStockClientMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor

public class StockClientAdapter implements IStockClientPort {

    private final IStockClient stockClient;
    private final IStockClientMapper stockClientMapper;

    @Override
    public void buyStock(Stock stock) {
        ResponseEntity<ResponseDto<StockDtoResponse>> response = stockClient.buyStock(stockClientMapper.toDtoRequest(stock));
        if (response.getStatusCode().is2xxSuccessful()) {
            return;
        }
        throw new RuntimeException("Failed to buy stock");
    }
}
