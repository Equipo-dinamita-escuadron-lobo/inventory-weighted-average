package com.kardex.infrastructure.adapters.input.rest.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.kardex.domain.model.Kardex;
import com.kardex.infrastructure.adapters.input.rest.dto.response.KardexDtoResponse;
import com.kardex.infrastructure.adapters.input.rest.dto.response.ListLastProductKardexDtoResponse;

@Mapper(componentModel = "spring")
public interface IKardexResponseMapper {
    KardexDtoResponse toDtoResponse(Kardex kardex);
    
    ListLastProductKardexDtoResponse toListLastProductKardexDtoResponse(Kardex kardex);
    
    List<ListLastProductKardexDtoResponse> toListLastProductKardexDtoResponseList(List<Kardex> kardexList);
}
