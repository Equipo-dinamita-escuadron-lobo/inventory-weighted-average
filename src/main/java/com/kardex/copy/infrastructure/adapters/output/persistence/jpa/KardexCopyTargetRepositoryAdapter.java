package com.kardex.copy.infrastructure.adapters.output.persistence.jpa;

import com.kardex.copy.application.output.IKardexCopyTargetRepositoryPort;
import com.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;
import com.kardex.infrastructure.adapters.output.jpa.repository.IKardexRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador JPA para IKardexCopyTargetRepositoryPort.
 * REQ-INVWA-01.
 */
@Component
@RequiredArgsConstructor
public class KardexCopyTargetRepositoryAdapter implements IKardexCopyTargetRepositoryPort {

    private final IKardexRepository jpaRepository;

    @Override
    @Transactional
    public KardexEntity guardar(KardexEntity entity) {
        return jpaRepository.save(entity);
    }
}
