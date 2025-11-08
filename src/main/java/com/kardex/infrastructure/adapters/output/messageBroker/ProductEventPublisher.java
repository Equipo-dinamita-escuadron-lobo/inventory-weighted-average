package com.kardex.infrastructure.adapters.output.messageBroker;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.kardex.domain.port.product.IProductEventPort;
import com.kardex.infrastructure.adapters.config.rabbitConfig.RabbitProductUsedConfig;
import com.kardex.infrastructure.adapters.output.messageBroker.dto.EventDto;
import com.kardex.infrastructure.adapters.output.messageBroker.enums.EventProductType;
import com.kardex.infrastructure.adapters.output.security.IJwtUtils;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventPublisher implements IProductEventPort{

    private final RabbitTemplate rabbitTemplate;
    private final IJwtUtils jwtUtils;


    @Override
    public void publishCreatedProductEvent(boolean isItBeingUsed) {
        EventDto<Boolean,EventProductType> event = new EventDto<>(EventProductType.USED, isItBeingUsed);
        log.info("Publishing product created event, isItBeingUsed: {}", isItBeingUsed);

        rabbitTemplate.convertAndSend(RabbitProductUsedConfig.PRODUCT_USED_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeader("x-jwt-token", jwtUtils.getToken());
            return message;
        });
    }


    
}