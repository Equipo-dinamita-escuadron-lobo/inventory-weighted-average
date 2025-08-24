package com.kardex.infrastructure.adapters.output.messageBroker;

import java.io.IOException;
import java.net.ConnectException;
import com.rabbitmq.client.Channel;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.dao.DataAccessException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;

import com.kardex.application.ports.input.IKardexCommandPort;
import com.kardex.domain.model.Kardex;
import com.kardex.infrastructure.adapters.config.RabbitWeightedAverageConfig;
import com.kardex.infrastructure.adapters.input.rest.dto.request.KardexDtoRequest;
import com.kardex.infrastructure.adapters.input.rest.mapper.IKardexRestMapper;
import com.kardex.infrastructure.adapters.output.messageBroker.dto.EventDto;
import com.kardex.infrastructure.adapters.output.messageBroker.enums.EventFactureType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FactureListener {
    private final IKardexCommandPort kardexCommandPort;
    private final IKardexRestMapper kardexRestMapper;


    @RabbitListener(queues =  RabbitWeightedAverageConfig.WEIGHTED_AVERAGE_QUEUE)
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void handleProductEvent( 
        EventDto<KardexDtoRequest, EventFactureType> event,
        Message message, 
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        try {
            // Safe check for the initial log  
            String kardexFactCode = getKardexNameSafely(event);  
            log.info("Processing message with delivery tag: {} for Kardex factCode: {}", 
                    deliveryTag, kardexFactCode);

            // Event validation
            if (event == null || event.getData() == null) {
                log.error("Received null event or null event data");
                channel.basicNack(deliveryTag, false, false); // Send to DLQ
                return;
            }

            // Process the event
            processEvent(event);

            // Manual message acknowledgment
            channel.basicAck(deliveryTag, false);
            log.info("Processed message for Kardex factCode: {}", kardexFactCode);


        } catch (Exception e) {
           // Safe check for the error log
            String kardexName = getKardexNameSafely(event);
            log.error("Error processing message for kardex: {}, error: {}", 
                    kardexName, e.getMessage(), e);

            try {
                // Check if it is a parsing or business logic error
                if (isRetryableError(e)) {
                    // Reject and requeue for retry
                    channel.basicNack(deliveryTag, false, true);
                    log.warn("Retrying message for kardex: {}", kardexName);
                } else {
                    // Non-recoverable error, send to DLQ
                    channel.basicNack(deliveryTag, false, false);
                    log.error("Sending message to DLQ for kardex: {}", kardexName);
                }
            } catch (IOException ioException) {
                log.error("Failed to nack message: {}", ioException.getMessage());
            }
        }
    }

    private void processEvent(EventDto<KardexDtoRequest, EventFactureType> event) {
        switch (event.getType()) {
            case PURCHASE:
                Kardex kardex = kardexRestMapper.toDomain(event.getData());
                kardexCommandPort.registerPurchase(kardex);
                log.info("Registering purchase in Kardex for product ID: {}", kardex.getIdProduct());
                break;
            case SALE:
                // Handle sale event
                break;
            case RETURNONSALE:
                // Handle return on sale event
                break;
            case RETURNONPURCHASE:
                // Handle return on purchase event
                break;
            default:
                throw new IllegalArgumentException("Unsupported event type: " + event.getType());
        }
    }

    private boolean isRetryableError(Exception e) {
        // Errors that may be temporary and worth retrying
        return e instanceof DataAccessException ||
               e instanceof TransactionException ||
               e instanceof ConnectException ||
               e instanceof TimeoutException;
    }

    // Listener for the Dead Letter Queue - for monitoring
    @RabbitListener(queues = RabbitWeightedAverageConfig.WEIGHTED_AVERAGE_DLQ)
    public void handleDeadLetterQueue(Message message) {
        try {
            String messageBody = new String(message.getBody());
            log.error("Message sent to DLQ: {}", messageBody);
            
        } catch (Exception e) {
            log.error("Error handling DLQ message: {}", e.getMessage(), e);
        }
    }

    private String getKardexNameSafely(EventDto<KardexDtoRequest, EventFactureType> event) {
        if (event == null || event.getData() == null) {
            return "unknown";
        }
        String name = event.getData().getFactCode().toString();
        return name != null ? name : "unnamed";
    }

}
