package com.kardex.infrastructure.adapters.output.messageBroker;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.kardex.domain.model.Product;
import com.kardex.domain.port.IMessageErrorHandlingPort;
import com.kardex.domain.port.IProductCommandRepositoryPort;
import com.kardex.infrastructure.adapters.config.rabbitConfig.RabbitProductConfig;
import com.kardex.infrastructure.adapters.output.messageBroker.base.AbstractMessageListener;
import com.kardex.infrastructure.adapters.output.messageBroker.dto.EventDto;
import com.kardex.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;
import com.kardex.infrastructure.adapters.output.messageBroker.enums.EventProductType;
import com.kardex.infrastructure.adapters.output.messageBroker.mapper.ProductBrokerMapper;
import com.kardex.infrastructure.adapters.output.messageBroker.util.JsonUtils;
import com.rabbitmq.client.Channel;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductListener extends AbstractMessageListener<EventDto<ProductAsyncDto, EventProductType>> {
    private final IProductCommandRepositoryPort productCommandPort;
    private final ProductBrokerMapper productBrokerMapper;
    private final IMessageErrorHandlingPort messageErrorHandlingPortImpl;
    
    @PostConstruct
    private void init() {
        this.messageErrorHandlingPort = messageErrorHandlingPortImpl;
    }

    @RabbitListener(queues = RabbitProductConfig.PRODUCT_KARDEX_QUEUE)
    public void handleProductEvent(
            EventDto<ProductAsyncDto, EventProductType> event, 
            Message message, 
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        
        handleMessage(event, channel, deliveryTag);
    }
    
    @Override
    protected void processEvent(EventDto<ProductAsyncDto, EventProductType> event) {
        ProductAsyncDto data = event.getData();
        String productName = data.getName() != null ? data.getName() : "unnamed";
        
        try {
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
        } catch (Exception e) {
            // Si hay error en la persistencia (ej: referencia duplicada), re-lanzar para que sea manejado por la clase padre
            log.error("Database operation failed for product {}: {}", productName, e.getMessage());
            throw e;
        }
    }

    @Override
    protected boolean isValidEvent(EventDto<ProductAsyncDto, EventProductType> event) {
        if (event == null) {
            log.warn("Event is null");
            return false;
        }
        
        if (event.getData() == null) {
            log.warn("Event data is null");
            return false;
        }
        
        ProductAsyncDto data = event.getData();
        
        // Validar campos obligatorios (todos menos presentation)
        if (data.getProductId() == null) {
            log.warn("ProductId is null - required field");
            return false;
        }
        
        if (data.getName() == null || data.getName().trim().isEmpty()) {
            log.warn("Name is null or empty - required field");
            return false;
        }
        
        if (data.getReference() == null || data.getReference().trim().isEmpty()) {
            log.warn("Reference is null or empty - required field");
            return false;
        }
        
        if (data.getEnterpriseId() == null || data.getEnterpriseId().trim().isEmpty()) {
            log.warn("EnterpriseId is null or empty - required field");
            return false;
        }
        
        return true;
    }

    @Override
    protected String getEntityType() {
        return "Product";
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
    protected String extractEventType(EventDto<ProductAsyncDto, EventProductType> event) {
        if (event == null) {
            return null;
        }
        
        return event.getType() != null ? event.getType().toString() : null;
    }

    @Override
    protected String convertEventToJson(EventDto<ProductAsyncDto, EventProductType> event) {
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
