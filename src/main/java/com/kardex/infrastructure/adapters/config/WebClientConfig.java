package com.kardex.infrastructure.adapters.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.kardex.infrastructure.adapters.output.multitenancy.interceptor.TenantInterceptor;

import lombok.RequiredArgsConstructor;

/**
 * @brief Configuration for HTTP client setup and load balancing
 * 
 * Provides centralized WebClient configuration for microservice communication
 * with integrated load balancing support through Spring Cloud.
 */
@RequiredArgsConstructor
@Configuration
public class WebClientConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addWebRequestInterceptor(tenantInterceptor);
    }
    /**
     * @brief Creates a load-balanced WebClient builder
     * 
     * The @LoadBalanced annotation enables Spring Cloud to resolve
     * service names registered in Eureka (e.g., "lb://Name").
     *
     * @return Configured WebClient.Builder with load balancing
     */
    @Bean
    @LoadBalanced
    WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}