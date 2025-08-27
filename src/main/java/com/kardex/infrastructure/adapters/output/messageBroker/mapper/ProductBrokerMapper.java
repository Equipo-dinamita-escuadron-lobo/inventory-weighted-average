package com.kardex.infrastructure.adapters.output.messageBroker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.kardex.domain.model.Product;
import com.kardex.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;

@Mapper(componentModel = "spring")
public interface ProductBrokerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "manager", ignore = true)
    Product toDomain(ProductAsyncDto productSyncDto);
}
