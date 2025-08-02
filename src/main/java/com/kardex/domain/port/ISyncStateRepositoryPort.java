package com.kardex.domain.port;

import java.time.Instant;
import java.util.Optional;

import com.kardex.domain.model.SyncState;

public interface ISyncStateRepositoryPort {
     Optional<SyncState> findBySyncTypeAndEnterpriseId(String syncType, String enterpriseId);
     Optional<Instant> findLastSyncFor(String syncType, String enterpriseId);
     void save(SyncState syncState);
}
