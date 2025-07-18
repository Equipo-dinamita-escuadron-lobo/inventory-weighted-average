package com.kardex.infrastructure.adapters.output.jpa.mapper;

import org.mapstruct.Mapper;

import com.kardex.domain.model.Kardex;
import com.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;

@Mapper(componentModel = "spring")
public interface KardexEntityMapper {
    
    KardexEntity toEntity(Kardex kardex);
    Kardex toDomain(KardexEntity kardexEntity);
}
