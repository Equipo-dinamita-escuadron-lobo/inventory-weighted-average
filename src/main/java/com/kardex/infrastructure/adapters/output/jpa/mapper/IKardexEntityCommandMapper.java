package com.kardex.infrastructure.adapters.output.jpa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.kardex.domain.model.Kardex;
import com.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;

@Mapper(componentModel = "spring")
public interface IKardexEntityCommandMapper {
    @Mapping(target = "tenantId", ignore = true)
    KardexEntity toEntity(Kardex kardex);
    
    Kardex toDomain(KardexEntity kardexEntity);
}
