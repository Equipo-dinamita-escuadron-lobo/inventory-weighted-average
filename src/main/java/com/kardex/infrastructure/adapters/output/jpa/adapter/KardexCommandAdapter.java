package com.kardex.infrastructure.adapters.output.jpa.adapter;

import org.springframework.stereotype.Repository;

import com.kardex.domain.model.Kardex;
import com.kardex.domain.port.IKardexCommandRepositoryPort;
import com.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;
import com.kardex.infrastructure.adapters.output.jpa.mapper.IKardexEntityCommandMapper;
import com.kardex.infrastructure.adapters.output.jpa.repository.IKardexRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class KardexCommandAdapter implements IKardexCommandRepositoryPort{

    private final IKardexEntityCommandMapper kardexEntityMapper;
    private final IKardexRepository kardexRepository;

    @Override
    public Kardex registerPurchase(Kardex kardex) {
        KardexEntity kardexEntity = kardexEntityMapper.toEntity(kardex);
        KardexEntity savedEntity = kardexRepository.save(kardexEntity);
        log.info("Registered purchase for product: {}", kardex.getIdProduct());
        return kardexEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Kardex registerSale(Kardex kardex) {
        KardexEntity kardexEntity = kardexEntityMapper.toEntity(kardex);
        KardexEntity savedEntity = kardexRepository.save(kardexEntity);
        log.info("Registered sale for product: {}", kardex.getIdProduct());
        return kardexEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Kardex registerReturnOnPurchase(Kardex kardex) {
        KardexEntity kardexEntity = kardexEntityMapper.toEntity(kardex);
        KardexEntity savedEntity = kardexRepository.save(kardexEntity);
        log.info("Registered return on purchase for product: {}", kardex.getIdProduct());
        return kardexEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Kardex registerReturnOnSale(Kardex kardex) {
        KardexEntity kardexEntity = kardexEntityMapper.toEntity(kardex);
        KardexEntity savedEntity = kardexRepository.save(kardexEntity);
        log.info("Registered return on sale for product: {}", kardex.getIdProduct());
        return kardexEntityMapper.toDomain(savedEntity);
    }

}
