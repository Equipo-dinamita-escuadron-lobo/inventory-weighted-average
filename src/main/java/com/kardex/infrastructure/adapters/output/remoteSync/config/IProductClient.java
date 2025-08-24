package com.kardex.infrastructure.adapters.output.remoteSync.config;

import java.time.Instant;
import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import com.kardex.infrastructure.adapters.output.remoteSync.dto.ProductSyncDto;

public interface IProductClient {
    
    @GetExchange("/api/products/sync/findByEnterpriseId/{enterpriseId}")
    List<ProductSyncDto> findAllProductsByEnterpriseId(
        @PathVariable String enterpriseId,
        @RequestParam Instant since
    );
}
