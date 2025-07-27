package com.kardex.application.ports.output;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import com.kardex.infrastructure.adapters.input.rest.dto.response.ProductSyncDto;

public interface IProductClient {
    
    @GetExchange("/api/products/sync/findAll/{enterpriseId}")
    List<ProductSyncDto> findAllProductsByEnterpriseId(@PathVariable String enterpriseId);
}
