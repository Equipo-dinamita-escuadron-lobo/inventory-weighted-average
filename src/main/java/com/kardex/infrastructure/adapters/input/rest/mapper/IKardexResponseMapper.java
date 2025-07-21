package com.kardex.infrastructure.adapters.input.rest.mapper;

import org.mapstruct.Mapper;

import com.kardex.domain.model.Kardex;
import com.kardex.infrastructure.adapters.input.rest.dto.response.KardexDtoResponse;

@Mapper(componentModel = "spring")
public interface IKardexResponseMapper {
    KardexDtoResponse toDtoResponse(Kardex kardex);
}
