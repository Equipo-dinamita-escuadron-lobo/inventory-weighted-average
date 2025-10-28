package com.kardex.infrastructure.adapters.output.remoteSync.adapter;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.kardex.domain.model.Kardex;
import com.kardex.domain.port.kardex.IKardexExternalClientPort;
import com.kardex.infrastructure.adapters.output.remoteSync.config.IKardexExternalClient;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.KardexExternalResponseDto;
import com.kardex.infrastructure.adapters.output.remoteSync.mapper.IKardexExternalClientMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Adapter for external kardex client integration
 * 
 * Implements the port for retrieving kardex data from external services
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KardexExternalClientAdapter implements IKardexExternalClientPort {

    private final IKardexExternalClientMapper kardexExternalClientMapper;
    private final IKardexExternalClient kardexExternalClient;

    @Override
    public List<Kardex> findKardexByEnterpriseId(String enterpriseId) {
        try {
            log.info("Fetching kardex records for enterprise: {}", enterpriseId);
            KardexExternalResponseDto response = kardexExternalClient.findKardexByEnterpriseId(enterpriseId);
            
            if (response == null || response.getData() == null) {
                log.warn("No data received from external kardex service for enterprise: {}", enterpriseId);
                throw new RuntimeException("No data received from external kardex service");
            }
            
            log.info("Retrieved {} kardex records from external service", response.getData().size());
            
            return response.getData()
                    .stream()
                    .map(kardexExternalClientMapper::toDomain)
                    .toList();
                    
        } catch (WebClientResponseException.ServiceUnavailable e) {
            log.error("Kardex external service is unavailable (503): {}", e.getMessage());
            throw new RuntimeException("Kardex external service is currently unavailable");
            
        } catch (WebClientResponseException.NotFound e) {
            log.warn("No kardex records found for enterprise: {}", enterpriseId);
            throw new RuntimeException("No kardex records found for the specified enterprise");
            
        } catch (WebClientResponseException e) {
            log.error("Error calling kardex external service: {} - {}", e.getStatusCode(), e.getStatusText());
            throw new RuntimeException("Error communicating with kardex external service: " + e.getStatusCode());
            
        } catch (Exception e) {
            log.error("Unexpected error calling kardex external service: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error communicating with kardex external service: " + e.getMessage());
        }
    }
}
