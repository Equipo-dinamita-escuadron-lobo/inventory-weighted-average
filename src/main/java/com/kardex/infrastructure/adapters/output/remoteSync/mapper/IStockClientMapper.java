package com.kardex.infrastructure.adapters.output.remoteSync.mapper;

import org.mapstruct.Mapper;

import com.kardex.domain.model.Stock;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.StockBuyDtoRequest;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.StockDtoResponse;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.StockSellDtoRequest;

@Mapper(componentModel = "spring")
public interface IStockClientMapper {

    StockBuyDtoRequest toDtoRequest(Stock stock);

    Stock toDomain(StockDtoResponse dto);

    StockSellDtoRequest toSellDtoRequest(Stock stock);

}
