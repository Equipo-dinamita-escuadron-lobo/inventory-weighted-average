package com.kardex.infrastructure.adapters.output.remoteSync.mapper;

import org.mapstruct.Mapper;

import com.kardex.domain.model.Stock;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.StockDtoRequest;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.StockDtoResponse;

@Mapper(componentModel = "spring")
public interface IStockClientMapper {

    StockDtoRequest toDtoRequest(Stock stock);

    Stock toDomain(StockDtoResponse dto);
}
