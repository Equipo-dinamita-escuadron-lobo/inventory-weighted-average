package com.kardex.infrastructure.adapters.output.remoteSync.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class StockDtoResponse {
    private Long id;

    private Long productId;

    private int quantity;

    private BigDecimal price;

    private boolean status;   
}
