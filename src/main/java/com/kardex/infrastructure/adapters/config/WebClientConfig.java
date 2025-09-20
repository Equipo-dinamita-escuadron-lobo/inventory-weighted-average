package com.kardex.infrastructure.adapters.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @brief Configuration for HTTP client setup and load balancing
 * 
 * Provides centralized WebClient configuration for microservice communication
 * with integrated load balancing support through Spring Cloud.
 */
@Configuration
public class WebClientConfig {

    /**
     * @brief Creates a load-balanced WebClient builder
     * 
     * The @LoadBalanced annotation enables Spring Cloud to resolve
     * service names registered in Eureka (e.g., "lb://STOCK").
     *
     * @return Configured WebClient.Builder with load balancing
     */
    @Bean
    @LoadBalanced
    WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}