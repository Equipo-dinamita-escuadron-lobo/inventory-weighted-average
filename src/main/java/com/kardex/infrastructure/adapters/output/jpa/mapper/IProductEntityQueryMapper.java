package com.kardex.infrastructure.adapters.output.jpa.mapper;

import org.mapstruct.Mapper;

import com.kardex.domain.model.Product;
import com.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;

@Mapper(componentModel = "spring")
public interface IProductEntityQueryMapper {
    Product toDomain(ProductEntity productEntity);
}
