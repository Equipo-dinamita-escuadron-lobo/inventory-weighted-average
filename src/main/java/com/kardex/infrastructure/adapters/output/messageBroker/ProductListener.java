package com.kardex.infrastructure.adapters.output.messageBroker;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.kardex.domain.model.Product;
import com.kardex.domain.port.IProductCommandRepositoryPort;
import com.kardex.infrastructure.adapters.config.rabbitConfig.RabbitProductConfig;
import com.kardex.infrastructure.adapters.output.messageBroker.base.AbstractMessageListener;
import com.kardex.infrastructure.adapters.output.messageBroker.dto.EventDto;
import com.kardex.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;
import com.kardex.infrastructure.adapters.output.messageBroker.enums.EventProductType;
import com.kardex.infrastructure.adapters.output.messageBroker.mapper.ProductBrokerMapper;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductListener extends AbstractMessageListener<EventDto<ProductAsyncDto, EventProductType>, EventProductType> {
    private final IProductCommandRepositoryPort productCommandPort;
    private final ProductBrokerMapper productBrokerMapper;

    @RabbitListener(queues = RabbitProductConfig.PRODUCT_KARDEX_QUEUE)
    public void handleProductEvent(
            EventDto<ProductAsyncDto, EventProductType> event, 
            Message message, 
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        
        handleMessage(event, message, channel, deliveryTag);
    }

    @Override
    protected void processEvent(EventDto<ProductAsyncDto, EventProductType> event) {
        ProductAsyncDto data = event.getData();
        String productName = data.getName() != null ? data.getName() : "unnamed";
        
        switch (event.getType()) {
            case CREATED:
                log.info("Creating new product: {}", productName);
                Product product = productBrokerMapper.toDomain(data);
                productCommandPort.save(product);
                log.info("Product created successfully: {}", product.getName());
                break;
                
            case UPDATED:
                log.info("Updating product: {}", productName);
                Product updatedProduct = productBrokerMapper.toDomain(data);
                productCommandPort.save(updatedProduct);
                log.info("Product updated successfully: {}", updatedProduct.getName());
                break;
                
            case DELETED:
                log.info("Deleting product: {}", productName);            
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported event type: " + event.getType());
        }
    }

    @Override
    protected boolean isValidEvent(EventDto<ProductAsyncDto, EventProductType> event) {
        return event != null && event.getData() != null;
    }

    @Override
    protected String getEntityIdentifierSafely(EventDto<ProductAsyncDto, EventProductType> event) {
        if (event == null || event.getData() == null) {
            return "unknown";
        }
        
        String name = event.getData().getName();
        return name != null ? name : "unnamed";
    }

    @Override
    protected String getEntityType() {
        return "Product";
    }

    // Listener for the Dead Letter Queue - for monitoring
    @RabbitListener(queues = RabbitProductConfig.PRODUCT_KARDEX_DLQ)
    public void handleProductDeadLetterQueue(Message message) {
        handleDeadLetterQueue(message);
    }
}
