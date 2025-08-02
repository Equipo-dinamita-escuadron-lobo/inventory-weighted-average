package com.kardex.infrastructure.adapters.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuración centralizada para WebClient y el cliente HTTP para el microservicio de Stock.
 */
@Configuration
public class WebClientConfig {

    /**
     * Crea un bean de WebClient.Builder que ya está preparado para el balanceo de carga.
     * La anotación @LoadBalanced es crucial para que Spring Cloud pueda resolver
     * los nombres de servicio registrados en Eureka (ej. "lb://STOCK").
     *
     * @return Un WebClient.Builder configurado.
     */
    @Bean
    @LoadBalanced
    WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}