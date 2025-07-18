package com.kardex.infrastructure.adapters.input.rest.dto.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KardexDtoResponse {
    private Long id;

    private Long quantity;

    private BigDecimal unitPrice;

    private String details;

    private Long balanceQuantity;

    private BigDecimal balanceUnitPrice;

    private ZonedDateTime date;
    
} 
