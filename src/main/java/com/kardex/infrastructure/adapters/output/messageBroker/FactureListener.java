package com.kardex.infrastructure.adapters.output.messageBroker;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.kardex.application.ports.input.IKardexCommandPort;
import com.kardex.domain.model.Kardex;
import com.kardex.infrastructure.adapters.config.rabbitConfig.RabbitWeightedAverageConfig;
import com.kardex.infrastructure.adapters.output.messageBroker.base.AbstractMessageListener;
import com.kardex.infrastructure.adapters.output.messageBroker.dto.EventDto;
import com.kardex.infrastructure.adapters.output.messageBroker.dto.KardexRabbitDto;
import com.kardex.infrastructure.adapters.output.messageBroker.enums.EventFactureType;
import com.kardex.infrastructure.adapters.output.messageBroker.mapper.IKardexRabbitMQMapper;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FactureListener extends AbstractMessageListener<EventDto<KardexRabbitDto, EventFactureType>> {
    private final IKardexCommandPort kardexCommandPort;
    private final IKardexRabbitMQMapper kardexRabbitMQRestMapper;

    @RabbitListener(queues = RabbitWeightedAverageConfig.WEIGHTED_AVERAGE_QUEUE)
    public void handleFactureEvent( 
        EventDto<KardexRabbitDto, EventFactureType> event,
        Message message, 
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        
        handleMessage(event, message, channel, deliveryTag);
    }

    @Override
    protected void processEvent(EventDto<KardexRabbitDto, EventFactureType> event) {
        switch (event.getType()) {
            case PURCHASE:
                Kardex kardex = kardexRabbitMQRestMapper.toDomain(event.getData());
                kardexCommandPort.registerPurchase(kardex);
                log.info("Registering purchase in Kardex for product ID: {}", kardex.getProductId());
                break;
            case SALE:
                Kardex kardexSale = kardexRabbitMQRestMapper.toDomain(event.getData());
                kardexCommandPort.registerSale(kardexSale);
                log.info("Registering sale in Kardex for product ID: {}", kardexSale.getProductId());
                break;
            case RETURNONSALE:
                Kardex kardexReturnOnSale = kardexRabbitMQRestMapper.toDomain(event.getData());
                kardexCommandPort.registerReturnOnSale(kardexReturnOnSale);
                log.info("Registering return on sale in Kardex for product ID: {}", kardexReturnOnSale.getProductId());
                break;
            case RETURNONPURCHASE:
                Kardex kardexReturnOnPurchase = kardexRabbitMQRestMapper.toDomain(event.getData());
                kardexCommandPort.registerReturnOnPurchase(kardexReturnOnPurchase);
                log.info("Registering return on purchase in Kardex for product ID: {}", kardexReturnOnPurchase.getProductId());
                break;
            default:
                throw new IllegalArgumentException("Unsupported event type: " + event.getType());
        }
    }

    @Override
    protected boolean isValidEvent(EventDto<KardexRabbitDto, EventFactureType> event) {
        return event != null && event.getData() != null;
    }

    @Override
    protected String getEntityType() {
        return "Kardex";
    }

    @Override
    protected String getEntityIdentifierSafely(EventDto<KardexRabbitDto, EventFactureType> event) {
        if (event == null || event.getData() == null) {
            return "unknown";
        }
        String factCode = event.getData().getFactCode() != null ? 
            event.getData().getFactCode().toString() : "unnamed";
        return factCode;
    }
}
