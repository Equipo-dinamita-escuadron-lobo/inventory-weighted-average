package com.kardex.infrastructure.adapters.output.jpa.mapper;

import org.mapstruct.Mapper;

import com.kardex.domain.model.MessageProcessingError;
import com.kardex.infrastructure.adapters.output.jpa.entity.MessageProcessingErrorEntity;

/**
 * @brief MapStruct mapper for MessageProcessingError entity conversions
 * 
 * Handles conversion between domain model and JPA entity for
 * message processing error objects.
 */
@Mapper(componentModel = "spring")
public interface IMessageProcessingErrorEntityMapper {
    
    /**
     * @brief Converts JPA entity to domain model
     * @param entity MessageProcessingError entity
     * @return MessageProcessingError domain model
     */
    MessageProcessingError toDomain(MessageProcessingErrorEntity entity);
    
    /**
     * @brief Converts domain model to JPA entity
     * @param domain MessageProcessingError domain model
     * @return MessageProcessingError entity
     */
    MessageProcessingErrorEntity toEntity(MessageProcessingError domain);
}