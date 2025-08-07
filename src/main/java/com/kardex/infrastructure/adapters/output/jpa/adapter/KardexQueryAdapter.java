package com.kardex.infrastructure.adapters.output.jpa.adapter;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.kardex.domain.model.Kardex;
import com.kardex.domain.port.IKardexQueryRepositoryPort;
import com.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;
import com.kardex.infrastructure.adapters.output.jpa.mapper.IKardexEntityQueryMapper;
import com.kardex.infrastructure.adapters.output.jpa.repository.IKardexRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KardexQueryAdapter implements IKardexQueryRepositoryPort {

    private final IKardexEntityQueryMapper kardexEntityMapper;
    private final IKardexRepository kardexRepository;

    @Override
    public Page<Kardex> findProductId(Long productId, Pageable pageable) {
        Page<KardexEntity> kardexEntities = kardexRepository.findByProductId(productId, pageable);
        return kardexEntities.map(kardexEntityMapper::toDomain);
    }

    @Override
    public List<Kardex> findByFactCode(Long factCode) {
        List<KardexEntity> kardexEntities = kardexRepository.findByFactCodeOrderByDateDesc(factCode);
        return kardexEntityMapper.toDomainList(kardexEntities);
    }

    @Override
    public Kardex getLatestKardexByProductId(Long productId) {
        KardexEntity kardexEntity = kardexRepository.findTopByProductIdOrderByDateDesc(productId);
        return kardexEntity != null ? kardexEntityMapper.toDomain(kardexEntity) : null;
    }
    
}
