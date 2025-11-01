package com.kardex.infrastructure.adapters.output.remoteSync.adapter;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.kardex.domain.model.Product;
import com.kardex.domain.port.common.IFormatterResultOutputPort;
import com.kardex.domain.port.product.IProductClientPort;
import com.kardex.infrastructure.adapters.output.remoteSync.config.IProductClient;
import com.kardex.infrastructure.adapters.output.remoteSync.mapper.IProductClientMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductClientAdapter implements IProductClientPort {

    private final IProductClientMapper productClientMapper;
    private final IProductClient productClient;
    private final IFormatterResultOutputPort formatterResultOutputPort;

    @Override
    public List<Product> findAllProductsByEnterpriseId(String enterpriseId, Instant since) {
        try {
            return productClient.findAllProductsByEnterpriseId(enterpriseId, since)
                    .stream()
                    .map(productClientMapper::toDomain)
                    .toList();
        } catch (WebClientResponseException.ServiceUnavailable e) {
            log.warn("Product service is unavailable (503)");
            formatterResultOutputPort.returnErrorGenericResponse(503, "Product service is currently unavailable");
        } catch (WebClientResponseException e) {
            log.warn("Error calling product service");
            formatterResultOutputPort.returnErrorGenericResponse(500, "Error communicating with product service");
        } catch (Exception e) {
            log.warn("Unexpected error calling product service");
            formatterResultOutputPort.returnErrorGenericResponse(500, "Unexpected error communicating with product service");
        }
        return List.of();
    }
    
}
