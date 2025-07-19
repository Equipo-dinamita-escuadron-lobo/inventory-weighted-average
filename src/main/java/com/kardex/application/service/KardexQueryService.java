package com.kardex.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.kardex.application.ports.input.IKardexQueryPort;
import com.kardex.domain.model.Kardex;
import com.kardex.domain.port.IKardexQueryRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KardexQueryService implements IKardexQueryPort {

    private final IKardexQueryRepositoryPort kardexQueryRepositoryPort;

    @Override
    public Page<Kardex> findProductId(Long productId, Pageable pageable) {
        return kardexQueryRepositoryPort.findProductId(productId, pageable);
    }
    
}
