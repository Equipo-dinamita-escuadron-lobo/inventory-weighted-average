package com.kardex.infrastructure.adapters.input.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.kardex.domain.model.Kardex;
import com.kardex.infrastructure.adapters.input.rest.dto.request.KardexPurchaseDtoRequest;
import com.kardex.infrastructure.adapters.input.rest.dto.request.KardexSaleDtoRequest;

@Mapper(componentModel = "spring")
public interface IKardexRestMapper {

    @Mapping(target = "product.id", source = "idProduct")
    @Mapping(target = "balanceQuantity", ignore = true)
    @Mapping(target = "balanceUnitPrice", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "id", ignore = true)
    Kardex toDomain(KardexPurchaseDtoRequest kardexPurchaseDtoRequest);

    @Mapping(target = "product.id", source = "idProduct")
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "balanceQuantity", ignore = true)
    @Mapping(target = "balanceUnitPrice", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "id", ignore = true)
    Kardex toDomain(KardexSaleDtoRequest kardexSaleDtoRequest);
}
