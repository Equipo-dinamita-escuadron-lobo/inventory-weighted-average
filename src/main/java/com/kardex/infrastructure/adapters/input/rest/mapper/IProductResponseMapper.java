package com.kardex.infrastructure.adapters.input.rest.mapper;

import org.mapstruct.Mapper;

import com.kardex.domain.model.Product;
import com.kardex.infrastructure.adapters.input.rest.dto.response.ProductDtoResponse;

@Mapper(componentModel = "spring")
public interface IProductResponseMapper {
    ProductDtoResponse toDtoResponse(Product product);
}
