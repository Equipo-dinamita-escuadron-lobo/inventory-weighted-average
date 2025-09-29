package com.kardex.domain.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Domain model for message processing errors
 * 
 * Represents errors that occur during message processing operations,
 * providing audit trail and debugging information.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class MessageProcessingError {
    
    private Long id;
    private String eventType;
    private String errorDescription;
    private String messageData;
    private Instant errorTimestamp;
    private String entityType;
    
}
