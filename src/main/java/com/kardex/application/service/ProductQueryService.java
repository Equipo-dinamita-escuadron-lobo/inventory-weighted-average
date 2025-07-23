package com.kardex.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kardex.application.ports.input.IProductQueryPort;
import com.kardex.domain.model.Product;
import com.kardex.domain.port.IProductQueryRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductQueryService implements IProductQueryPort {

    private final IProductQueryRepositoryPort productQueryRepository;

    @Override
    public List<Product> findAll(String enterpriseId) {
        return productQueryRepository.findAll(enterpriseId);
    }
    
}
