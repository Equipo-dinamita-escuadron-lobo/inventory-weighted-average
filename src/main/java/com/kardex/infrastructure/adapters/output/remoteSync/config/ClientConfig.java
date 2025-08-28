package com.kardex.infrastructure.adapters.output.remoteSync.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.kardex.infrastructure.adapters.output.messageBroker.aspect.JwtRabbitUtils;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableConfigurationProperties(ClientProperties.class)
@Slf4j
public class ClientConfig {
    
    /**
     * Método factory privado y genérico para crear cualquier cliente proxy.
     * Centraliza la lógica de construcción del WebClient y el HttpServiceProxyFactory.
     */
    private <T> T createWebClientProxy(WebClient.Builder webClientBuilder, String baseUrl, Class<T> clientInterface) {
        // Validar que baseUrl no sea null o vacío
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("BaseURL cannot be null or empty for client: " + clientInterface.getSimpleName());
        }
        
        log.info("Creating WebClient proxy for {} with baseUrl: {}", clientInterface.getSimpleName(), baseUrl);
        
        // 1. Construye una instancia de WebClient específica para este cliente
        WebClient webClient = webClientBuilder
                .baseUrl(baseUrl)
                .filter(jwtPropagationFilter())
                .build();

        // 2. Crea el adaptador y la fábrica del proxy
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        // 3. Crea y devuelve el cliente
        return factory.createClient(clientInterface);
    }

    @Bean
    IStockClient stockClient(WebClient.Builder webClientBuilder, ClientProperties properties) {
        String baseUrl = properties.getStock().getBaseUrl();
        return createWebClientProxy(webClientBuilder, baseUrl, IStockClient.class);
    }

    @Bean
    IProductClient productClient(WebClient.Builder webClientBuilder, ClientProperties properties) {
        String baseUrl = properties.getProducts().getBaseUrl();
        return createWebClientProxy(webClientBuilder, baseUrl, IProductClient.class);
    }

    private ExchangeFilterFunction jwtPropagationFilter() {
        return (clientRequest, next) -> {
            if (JwtRabbitUtils.getJwtToken() != null) {
                final String tokenValue = JwtRabbitUtils.getJwtToken();
                log.debug("Using JWT token from custom context");
                
                ClientRequest newRequest = ClientRequest.from(clientRequest)
                        .headers(headers -> headers.setBearerAuth(tokenValue))
                        .build();

                return next.exchange(newRequest);
            }
            
            log.debug("No JWT token available for propagation");
            return next.exchange(clientRequest);
        };
    }
}