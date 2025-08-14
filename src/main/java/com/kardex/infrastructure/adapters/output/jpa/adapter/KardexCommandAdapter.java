package com.kardex.infrastructure.adapters.output.jpa.adapter;

import org.springframework.stereotype.Component;

import com.kardex.domain.model.Kardex;
import com.kardex.domain.port.IKardexCommandRepositoryPort;
import com.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;
import com.kardex.infrastructure.adapters.output.jpa.mapper.IKardexEntityCommandMapper;
import com.kardex.infrastructure.adapters.output.jpa.repository.IKardexRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KardexCommandAdapter implements IKardexCommandRepositoryPort{

    private final IKardexEntityCommandMapper kardexEntityMapper;
    private final IKardexRepository kardexRepository;

    @Override
    public Kardex registerPurchase(Kardex kardex) {
        KardexEntity kardexEntity = kardexEntityMapper.toEntity(kardex);

        return kardexEntityMapper.toDomain(kardexRepository.save(kardexEntity));
    }

    @Override
    public Kardex registerSale(Kardex kardex) {
        KardexEntity kardexEntity = kardexEntityMapper.toEntity(kardex);

        return kardexEntityMapper.toDomain(kardexRepository.save(kardexEntity));
    }

    @Override
    public Kardex registerReturnOnPurchase(Kardex kardex) {
        KardexEntity kardexEntity = kardexEntityMapper.toEntity(kardex);
        
        return kardexEntityMapper.toDomain(kardexRepository.save(kardexEntity));
    }

    @Override
    public Kardex registerReturnOnSale(Kardex kardex) {
        KardexEntity kardexEntity = kardexEntityMapper.toEntity(kardex);
        
        return kardexEntityMapper.toDomain(kardexRepository.save(kardexEntity));
    }

}
