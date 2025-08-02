package com.kardex.infrastructure.adapters.output.jpa.adapter;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.kardex.domain.model.SyncState;
import com.kardex.domain.port.ISyncStateRepositoryPort;
import com.kardex.infrastructure.adapters.output.jpa.entity.SyncStateEntity;
import com.kardex.infrastructure.adapters.output.jpa.mapper.ISyncStateEntityMapper;
import com.kardex.infrastructure.adapters.output.jpa.repository.ISyncStateRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SyncStatePortAdapter implements ISyncStateRepositoryPort{

    private final ISyncStateRepository syncStateRepository;
    private final ISyncStateEntityMapper syncStateEntityMapper;

    @Override
    public Optional<SyncState> findBySyncTypeAndEnterpriseId(String syncType, String enterpriseId) {
        Optional<SyncStateEntity> entity = syncStateRepository.findBySyncTypeAndEnterpriseId(syncType, enterpriseId);
        return entity.map(syncStateEntityMapper::toDomain);
    }

    @Override
    public Optional<Instant> findLastSyncFor(String syncType, String enterpriseId) {
        Optional<Instant> lastSync = syncStateRepository.findLastSyncFor(syncType, enterpriseId);
        return lastSync;
    }

    @Override
    public void save(SyncState syncState) {
        SyncStateEntity entity = syncStateEntityMapper.toEntity(syncState);
        syncStateRepository.save(entity);
    }       
}
    

