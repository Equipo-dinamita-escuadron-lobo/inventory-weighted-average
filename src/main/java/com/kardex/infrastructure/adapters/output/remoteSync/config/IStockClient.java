package com.kardex.infrastructure.adapters.output.remoteSync.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PutExchange;

import com.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.StockBuyDtoRequest;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.StockDtoResponse;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.StockSellDtoRequest;

public interface IStockClient {

    @PutExchange("/api/stock/buy")
    ResponseEntity<ResponseDto<StockDtoResponse>> buyStock(@RequestBody StockBuyDtoRequest stockDtoRequest);

    ResponseEntity<ResponseDto<StockDtoResponse>> sellStock(@RequestBody StockSellDtoRequest stockDtoRequest);
}
