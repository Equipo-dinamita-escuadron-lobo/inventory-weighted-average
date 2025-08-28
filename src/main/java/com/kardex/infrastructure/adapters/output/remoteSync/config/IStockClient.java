package com.kardex.infrastructure.adapters.output.remoteSync.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PutExchange;

import com.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.StockDtoRequest;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.StockDtoResponse;

public interface IStockClient {

    @PutExchange("/api/stock/buy")
    ResponseEntity<ResponseDto<StockDtoResponse>> buyStock(@RequestBody StockDtoRequest stockDtoRequest);
}
