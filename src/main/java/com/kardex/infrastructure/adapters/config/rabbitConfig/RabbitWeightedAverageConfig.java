package com.kardex.infrastructure.adapters.config.rabbitConfig;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@Profile("!test")
public class RabbitWeightedAverageConfig {
    public static final String WEIGHTED_AVERAGE_EXCHANGE = "weighted.average.exchange";
    public static final String WEIGHTED_AVERAGE_QUEUE = "weighted.average.queue";

    @Bean
    Queue weightedAverageQueue() {
        return QueueBuilder.durable(WEIGHTED_AVERAGE_QUEUE).build();
    }

    @Bean
    FanoutExchange weightedAverageExchange() {
        return new FanoutExchange(WEIGHTED_AVERAGE_EXCHANGE, true, false);
    }

    @Bean
    Binding weightedAverageQueueBinding() {
        return BindingBuilder.bind(weightedAverageQueue()).to(weightedAverageExchange());
    }
}
