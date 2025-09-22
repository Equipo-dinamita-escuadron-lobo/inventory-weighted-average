package com.kardex.infrastructure.adapters.output.remoteSync.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PutExchange;

import com.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.StockBuyDtoRequest;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.StockDtoResponse;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.StockSellDtoRequest;

/**
 * @brief HTTP client interface for stock service integration
 * 
 * Provides remote service calls for stock operations using Spring's
 * declarative HTTP interface with automatic service discovery.
 */
public interface IStockClient {

    /**
     * @brief Calls external stock service to increase inventory
     * @param stockDtoRequest Stock purchase request data
     * @return Response with updated stock information
     */
    @PutExchange("/api/stock/buy")
    ResponseEntity<ResponseDto<StockDtoResponse>> buyStock(@RequestBody StockBuyDtoRequest stockDtoRequest);

    /**
     * @brief Calls external stock service to decrease inventory
     * @param stockDtoRequest Stock sale request data
     * @return Response with updated stock information
     */
    @PutExchange("/api/stock/sell")
    ResponseEntity<ResponseDto<StockDtoResponse>> sellStock(@RequestBody StockSellDtoRequest stockDtoRequest);
}
