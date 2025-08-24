package com.kardex.infrastructure.adapters.output.remoteSync.mapper;

import org.mapstruct.Mapper;

import com.kardex.domain.model.Product;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.ProductSyncDto;

@Mapper(componentModel = "spring")
public interface IProductClientMapper {

    ProductSyncDto toDto(Product product);

    Product toDomain(ProductSyncDto dto);
}
