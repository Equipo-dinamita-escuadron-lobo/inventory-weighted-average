package com.kardex.infrastructure.adapters.output.jpa.adapter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.kardex.domain.model.Kardex;
import com.kardex.domain.port.IKardexQueryRepositoryPort;
import com.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;
import com.kardex.infrastructure.adapters.output.jpa.mapper.IKardexEntityQueryMapper;
import com.kardex.infrastructure.adapters.output.jpa.repository.IKardexRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class KardexQueryAdapter implements IKardexQueryRepositoryPort {

    private final IKardexEntityQueryMapper kardexEntityMapper;
    private final IKardexRepository kardexRepository;

    @Override
    public Page<Kardex> findProductIdAndDate(Long productId, Pageable pageable, LocalDate startDate, LocalDate endDate) {
        ZonedDateTime startDateTime = startDate.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endDateTime = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault());
        Page<KardexEntity> kardexEntities = kardexRepository.findByProductIdAndDateBetween(productId, startDateTime, endDateTime, pageable);
        return kardexEntities.map(kardexEntityMapper::toDomain);
    }

    @Override
    public Page<Kardex> findProductId(Long productId, Pageable pageable) {
        Page<KardexEntity> kardexEntities = kardexRepository.findByProductId(productId, pageable);
        return kardexEntities.map(kardexEntityMapper::toDomain);
    }

    @Override
    public List<Kardex> findByFactCodeAndProductId(Long factCode, Long productId) {
        List<KardexEntity> kardexEntities = kardexRepository.findByFactCodeAndProductId(factCode, productId);
        return kardexEntityMapper.toDomainList(kardexEntities);
    }

    @Override
    public Kardex getLatestKardexByProductId(Long productId) {
        KardexEntity kardexEntity = kardexRepository.findTopByProductIdOrderByDateDesc(productId);
        return kardexEntity != null ? kardexEntityMapper.toDomain(kardexEntity) : null;
    }
    
}
