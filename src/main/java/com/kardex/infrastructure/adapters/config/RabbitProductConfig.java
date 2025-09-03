package com.kardex.infrastructure.adapters.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;

@Configuration
@Slf4j
@Profile("!test")
public class RabbitProductConfig {
    public static final String PRODUCT_EXCHANGE = "product.exchange";
    public static final String PRODUCT_KARDEX_QUEUE = "product.kardex.queue";
    
    // Dead Letter Queue configuration
    public static final String PRODUCT_KARDEX_DLQ = "product.kardex.dlq";
    public static final String PRODUCT_KARDEX_DLX = "product.kardex.dlx";

    // Dead Letter Exchange
    @Bean
    FanoutExchange productKardexDlx() {
        return new FanoutExchange(PRODUCT_KARDEX_DLX, true, false);
    }

    // Dead Letter Queue - for domain/business rule violations (no retry)
    @Bean
    Queue productKardexDlq() {
        return QueueBuilder.durable(PRODUCT_KARDEX_DLQ).build();
    }

    // Main Queue with DLQ configuration - for product synchronization
    @Bean
    Queue productKardexQueue() {
        return QueueBuilder.durable(PRODUCT_KARDEX_QUEUE)
                .withArgument("x-dead-letter-exchange", PRODUCT_KARDEX_DLX)
                .withArgument("x-dead-letter-routing-key", "")
                // TTL for product events - can be higher as order is less critical
                .withArgument("x-message-ttl", 600000) // 10 minutes TTL
                .build();
    }

    @Bean
    FanoutExchange productExchange() {
        return new FanoutExchange(PRODUCT_EXCHANGE, true, false);
    }

    @Bean
    Binding productKardexQueueBinding() {
        return BindingBuilder.bind(productKardexQueue()).to(productExchange());
    }

    @Bean
    Binding productKardexDlqBinding() {
        return BindingBuilder.bind(productKardexDlq()).to(productKardexDlx());
    }
}
