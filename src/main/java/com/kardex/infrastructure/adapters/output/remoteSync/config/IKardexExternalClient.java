package com.kardex.infrastructure.adapters.output.remoteSync.config;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import com.kardex.infrastructure.adapters.output.remoteSync.dto.KardexExternalResponseDto;

/**
 * @brief HTTP client for kardex external synchronization
 * 
 * Client interface for retrieving kardex data from remote services
 */
public interface IKardexExternalClient {
    
    /**
     * @brief Retrieves kardex records by enterprise ID
     * @param enterpriseId The enterprise identifier
     * @return Response with list of kardex records
     */
    @GetExchange("/api/kardex/peps/last-kardex-all-products/{enterpriseId}")
    KardexExternalResponseDto findKardexByEnterpriseId(@PathVariable String enterpriseId);
}
