package com.kardex.infrastructure.adapters.input.rest.dto.request;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KardexPurchaseDtoRequest {
    private Long quantity;

    private BigDecimal unitPrice;

    private String details;

    private Long idProduct;
}
