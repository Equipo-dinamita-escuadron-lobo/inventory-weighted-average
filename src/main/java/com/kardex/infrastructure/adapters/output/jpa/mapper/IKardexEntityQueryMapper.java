package com.kardex.infrastructure.adapters.output.jpa.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.kardex.domain.model.Kardex;
import com.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;

@Mapper(componentModel = "spring")
public interface IKardexEntityQueryMapper {

    @Mapping(target = "product", ignore = true)
    Kardex toDomain(KardexEntity kardexEntity);

    List<Kardex> toDomainList(List<KardexEntity> kardexEntities);
}
