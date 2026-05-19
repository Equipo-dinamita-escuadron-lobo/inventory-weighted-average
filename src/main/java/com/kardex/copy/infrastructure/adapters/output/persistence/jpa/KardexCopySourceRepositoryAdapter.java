package com.kardex.copy.infrastructure.adapters.output.persistence.jpa;

import com.kardex.copy.application.output.IKardexCopySourceRepositoryPort;
import com.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;
import com.kardex.infrastructure.adapters.output.jpa.repository.IKardexRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Adaptador JPA para IKardexCopySourceRepositoryPort.
 * REQ-INVWA-01.
 */
@Component
@RequiredArgsConstructor
public class KardexCopySourceRepositoryAdapter implements IKardexCopySourceRepositoryPort {

    private final IKardexRepository jpaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<KardexEntity> findByProductId(Long productId) {
        // Obtener todos los movimientos — size grande para carga completa
        return jpaRepository.findByProductId(productId, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }
}
