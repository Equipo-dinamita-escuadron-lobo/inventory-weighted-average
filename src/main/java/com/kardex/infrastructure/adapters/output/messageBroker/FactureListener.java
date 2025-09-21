package com.kardex.infrastructure.adapters.output.messageBroker;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.kardex.application.ports.input.IKardexCommandPort;
import com.kardex.domain.model.Kardex;
import com.kardex.domain.port.IMessageErrorHandlingPort;
import com.kardex.infrastructure.adapters.config.rabbitConfig.RabbitWeightedAverageConfig;
import com.kardex.infrastructure.adapters.output.messageBroker.base.AbstractMessageListener;
import com.kardex.infrastructure.adapters.output.messageBroker.dto.EventDto;
import com.kardex.infrastructure.adapters.output.messageBroker.dto.KardexRabbitDto;
import com.kardex.infrastructure.adapters.output.messageBroker.enums.EventFactureType;
import com.kardex.infrastructure.adapters.output.messageBroker.mapper.IKardexRabbitMQMapper;
import com.kardex.infrastructure.adapters.output.messageBroker.util.JsonUtils;
import com.rabbitmq.client.Channel;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief RabbitMQ listener for facture (invoice) related events
 * 
 * Handles various inventory movement events such as purchases, sales,
 * and returns, with robust error handling and validation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FactureListener extends AbstractMessageListener<EventDto<KardexRabbitDto, EventFactureType>> {
    private final IKardexCommandPort kardexCommandPort;
    private final IKardexRabbitMQMapper kardexRabbitMQRestMapper;
    private final IMessageErrorHandlingPort messageErrorHandlingPortImpl;
    
    @PostConstruct
    private void init() {
        this.messageErrorHandlingPort = messageErrorHandlingPortImpl;
        // Implement specific recovery strategy for kardex events if needed
    }

    /**
     * @brief Handles facture events from the RabbitMQ queue
     * @param event The facture event DTO
     * @param message The raw RabbitMQ message
     * @param channel The RabbitMQ channel
     * @param deliveryTag The message delivery tag
     */
    @RabbitListener(queues = RabbitWeightedAverageConfig.WEIGHTED_AVERAGE_QUEUE)
    public void handleFactureEvent( 
        EventDto<KardexRabbitDto, EventFactureType> event,
        Message message, 
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        
        handleMessage(event, channel, deliveryTag);
    }

    /**
     * @brief Processes the facture event based on its type
     * @param event The facture event to process
     */
    @Override
    protected void processEvent(EventDto<KardexRabbitDto, EventFactureType> event) {
        try {
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
        } catch (Exception e) {
            // Re-throw to be handled by the parent class if persistence fails
            log.error("Database operation failed for kardex operation: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * @brief Validates the integrity of the facture event data
     * @param event The facture event to validate
     * @return True if the event is valid, false otherwise
     */
    @Override
    protected boolean isValidEvent(EventDto<KardexRabbitDto, EventFactureType> event) {
        if (event == null) {
            log.warn("Event is null");
            return false;
        }
        
        if (event.getData() == null) {
            log.warn("Event data is null");
            return false;
        }
        
        KardexRabbitDto data = event.getData();
        
        // Validate required fields
        if (data.getQuantity() == null || data.getQuantity() <= 0) {
            log.warn("Quantity is null or invalid - required field");
            return false;
        }
        
        if (data.getFactCode() == null) {
            log.warn("FactCode is null - required field");
            return false;
        }
        
        if (data.getProductId() == null) {
            log.warn("ProductId is null - required field");
            return false;
        }
        
        // For purchase operations, unitPrice is required
        if ((event.getType() == EventFactureType.PURCHASE || event.getType() == EventFactureType.RETURNONPURCHASE) 
            && (data.getUnitPrice() == null || data.getUnitPrice().compareTo(java.math.BigDecimal.ZERO) <= 0)) {
            log.warn("UnitPrice is null or invalid for purchase operation - required field");
            return false;
        }
        
        return true;
    }

    @Override
    protected String getEntityType() {
        return "Kardex";
    }

    @Override
    protected String extractEventType(EventDto<KardexRabbitDto, EventFactureType> event) {
        if (event == null) {
            return null;
        }
        
        return event.getType() != null ? event.getType().toString() : null;
    }

    @Override
    protected String convertEventToJson(EventDto<KardexRabbitDto, EventFactureType> event) {
        if (event == null) {
            return "{\"error\": \"Event is null\"}";
        }
        
        if (event.getData() == null) {
            return "{\"error\": \"Event data is null\", \"eventType\": \"" + 
                   (event.getType() != null ? event.getType().toString() : "null") + "\"}";
        }
        
        return JsonUtils.toJsonWithNullHandling(event.getData());
    }
}
