package com.kardex.infrastructure.adapters.output.remoteSync.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.kardex.domain.model.Kardex;
import com.kardex.infrastructure.adapters.input.rest.dto.request.KardexBatchDtoRequest;

/**
 * @brief Mapper for converting external kardex DTOs to domain models
 */
@Mapper(componentModel = "spring")
public interface IKardexExternalClientMapper {
    
    /**
     * @brief Converts external DTO to domain model
     * @param dto External kardex DTO
     * @return Domain kardex model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    Kardex toDomain(KardexBatchDtoRequest dto);
}
