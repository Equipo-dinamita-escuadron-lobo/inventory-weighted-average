package com.kardex.infrastructure.adapters.output.remoteSync.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class StockDtoRequest {

    private Long productId;

    private String enterpriseId;

    private int quantity;

    private BigDecimal price;
}
