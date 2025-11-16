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
    private Config config = new Config();
    private KardexExternal kardexExternal = new KardexExternal();

    @Data
    public static class Stock {
        private String baseUrl;
    }

    @Data
    public static class Products {
        private String baseUrl;
    }

    @Data
    public static class Config {
        private String baseUrl;
    }

    @Data
    public static class KardexExternal {
        private String baseUrl;
    }

    @PostConstruct
    public void validate() {
        log.info("Validating client properties...");
        log.info("Stock base URL: {}", stock.getBaseUrl());
        log.info("Products base URL: {}", products.getBaseUrl());
        log.info("Config base URL: {}", config.getBaseUrl());
        log.info("Kardex External base URL: {}", kardexExternal.getBaseUrl());
        
        if (stock.getBaseUrl() == null || stock.getBaseUrl().trim().isEmpty()) {
            throw new IllegalStateException("Stock base URL not configured. Expected 'services.stock.base-url'");
        }
        if (products.getBaseUrl() == null || products.getBaseUrl().trim().isEmpty()) {
            throw new IllegalStateException("Products base URL not configured. Expected 'services.products.base-url'");
        }
        if (config.getBaseUrl() == null || config.getBaseUrl().trim().isEmpty()) {
            throw new IllegalStateException("Config base URL not configured. Expected 'services.config.base-url'");
        }
        if (kardexExternal.getBaseUrl() == null || kardexExternal.getBaseUrl().trim().isEmpty()) {
            throw new IllegalStateException("Kardex External base URL not configured. Expected 'services.kardex-external.base-url'");
        }
        
        log.info("Client properties validated successfully");
    }
}