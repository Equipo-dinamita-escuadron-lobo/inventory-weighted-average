package com.kardex.infrastructure.adapters.output.messageBroker;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.kardex.domain.model.Product;
import com.kardex.domain.port.IProductCommandRepositoryPort;
import com.kardex.infrastructure.adapters.config.RabbitConfig;
import com.kardex.infrastructure.adapters.output.messageBroker.dto.EventDto;
import com.kardex.infrastructure.adapters.output.messageBroker.dto.ProductSyncDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductListener{
    private final IProductCommandRepositoryPort productCommandPort;

    @RabbitListener(queues = RabbitConfig.PRODUCT_STOCK_QUEUE)
    public void handleStockEvent(EventDto<ProductSyncDto> event, Message message) {
        log.info("Received stock event: {}", event.getData());

        switch (event.getType()) {
            case CREATED:
                Product product = Product.builder()
                        .idProduct(event.getData().getProductId())
                        .reference(event.getData().getReference())
                        .name(event.getData().getName())
                        .enterpriseId(event.getData().getEnterpriseId())
                        .build();

                productCommandPort.save(product);             
                break;
            case UPDATED:
                
                break;
            case DELETED:
                
                break;
        }
    }
}