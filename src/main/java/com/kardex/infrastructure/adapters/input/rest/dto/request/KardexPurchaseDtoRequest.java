package com.kardex.infrastructure.adapters.input.rest.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KardexPurchaseDtoRequest {
    @NotNull(message = "The field 'quantity' cannot be null")
    private Long quantity;

    @NotNull(message = "The field 'factCode' cannot be null")
    private Long factCode;

    @NotNull(message = "The field 'unitPrice' cannot be null")
    private BigDecimal unitPrice;

    private String details;

    @NotNull(message = "The field 'idProduct' cannot be null")
    private Long idProduct;
}
