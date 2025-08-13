package com.kardex.infrastructure.adapters.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Centralized configuration for WebClient and the HTTP client for the Stock microservice.
 */
@Configuration
public class WebClientConfig {

    /**
     * Creates a WebClient.Builder bean already set up for load balancing.
     * The @LoadBalanced annotation is crucial for Spring Cloud to resolve
     * service names registered in Eureka (e.g., "lb://STOCK").
     *
     * @return A configured WebClient.Builder.
     */
    @Bean
    @LoadBalanced
    WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}