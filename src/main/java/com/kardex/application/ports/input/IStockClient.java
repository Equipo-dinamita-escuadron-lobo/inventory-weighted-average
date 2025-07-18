package com.kardex.application.ports.input;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PutExchange;

import com.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import com.kardex.infrastructure.adapters.input.rest.dto.request.StockDtoRequest;
import com.kardex.infrastructure.adapters.input.rest.dto.response.StockDtoResponse;

public interface IStockClient {

    @PutExchange("/api/stock/buy")
    ResponseEntity<ResponseDto<StockDtoResponse>> buyStock(@RequestBody StockDtoRequest stockDtoRequest);
}
