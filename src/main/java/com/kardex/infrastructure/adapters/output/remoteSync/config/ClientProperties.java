package com.kardex.infrastructure.adapters.output.remoteSync.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@ConfigurationProperties(prefix = "services")
@Data
@Slf4j
public class ClientProperties {

    private Stock stock = new Stock();
    private Products products = new Products();

    @Data
    public static class Stock {
        private String baseUrl;
    }

    @Data
    public static class Products {
        private String baseUrl;
    }

    @PostConstruct
    public void validate() {
        log.info("Validating client properties...");
        log.info("Stock base URL: {}", stock.getBaseUrl());
        log.info("Products base URL: {}", products.getBaseUrl());
        
        if (stock.getBaseUrl() == null || stock.getBaseUrl().trim().isEmpty()) {
            throw new IllegalStateException("Stock base URL not configured. Expected 'services.stock.base-url'");
        }
        if (products.getBaseUrl() == null || products.getBaseUrl().trim().isEmpty()) {
            throw new IllegalStateException("Products base URL not configured. Expected 'services.products.base-url'");
        }
        
        log.info("Client properties validated successfully");
    }
}