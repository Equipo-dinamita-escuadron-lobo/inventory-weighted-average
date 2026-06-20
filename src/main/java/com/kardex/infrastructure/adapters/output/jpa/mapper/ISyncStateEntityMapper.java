package com.kardex.infrastructure.adapters.output.jpa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.kardex.domain.model.SyncState;
import com.kardex.infrastructure.adapters.output.jpa.entity.SyncStateEntity;

@Mapper(componentModel = "spring")
public interface ISyncStateEntityMapper {
    SyncState toDomain(SyncStateEntity entity);
    
    @Mapping(target = "tenantId", ignore = true)
    SyncStateEntity toEntity(SyncState domain);
}
