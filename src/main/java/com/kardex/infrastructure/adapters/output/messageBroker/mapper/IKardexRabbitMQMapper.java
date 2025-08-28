package com.kardex.infrastructure.adapters.output.messageBroker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.kardex.domain.model.Kardex;
import com.kardex.infrastructure.adapters.output.messageBroker.dto.KardexRabbitDto;

@Mapper(componentModel = "spring")
public interface IKardexRabbitMQMapper {

    @Mapping(target = "balanceQuantity", ignore = true)
    @Mapping(target = "balanceUnitPrice", ignore = true)
    @Mapping(target = "totalBalance", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "id", ignore = true)
    Kardex toDomain(KardexRabbitDto kardexPurchaseDtoRequest);


}
