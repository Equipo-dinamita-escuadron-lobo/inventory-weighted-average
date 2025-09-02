package com.kardex.infrastructure.adapters.output.remoteSync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class StockSellDtoRequest {

    private Long productId;

    private String enterpriseId;

    private int quantity;
}
