package com.kardex.infrastructure.adapters.output.jpa.adapter;

import org.springframework.stereotype.Component;

import com.kardex.domain.model.Kardex;
import com.kardex.domain.port.IKardexCommandRepositoryPort;
import com.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;
import com.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;
import com.kardex.infrastructure.adapters.output.jpa.mapper.KardexEntityMapper;
import com.kardex.infrastructure.adapters.output.jpa.repository.IKardexRepository;
import com.kardex.infrastructure.adapters.output.jpa.repository.IProductRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KardexCommandAdapter implements IKardexCommandRepositoryPort{

    private final KardexEntityMapper kardexEntityMapper;
    private final IKardexRepository kardexRepository;
    private final IProductRepository productRepository;

    @Override
    public Kardex registerPurchase(Kardex kardex) {
        ProductEntity productEntity = productRepository.getReferenceById(kardex.getProduct().getId());
        KardexEntity kardexEntity = kardexEntityMapper.toEntity(kardex);
        kardexEntity.setProduct(productEntity);

        return kardexEntityMapper.toDomain(kardexRepository.save(kardexEntity));
    }

    @Override
    public Kardex registerSale(Kardex kardex) {
        ProductEntity productEntity = productRepository.getReferenceById(kardex.getProduct().getId());
        KardexEntity kardexEntity = kardexEntityMapper.toEntity(kardex);
        kardexEntity.setProduct(productEntity);

        return kardexEntityMapper.toDomain(kardexRepository.save(kardexEntity));
    }

    @Override
    public Kardex getLatestKardexByProductId(Long productId) {
        KardexEntity kardexEntity = kardexRepository.findTopByProductIdOrderByDateDesc(productId);
        return kardexEntity != null ? kardexEntityMapper.toDomain(kardexEntity) : null;
    }
    
}
