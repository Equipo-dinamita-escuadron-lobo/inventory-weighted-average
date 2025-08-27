package com.kardex.infrastructure.adapters.input.rest.dto.request;

import java.math.BigDecimal;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for receiving purchase operations in the Kardex system
 * Contains fields necessary for registering a product purchase
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KardexDtoRequest {
    @NotNull(message = "The field 'quantity' cannot be null")
    @Positive(message = "The quantity must be positive")
    private Long quantity;

    @NotNull(message = "The field 'factCode' cannot be null")
    @Positive(message = "The factCode must be positive")
    private Long factCode;

    @NotNull(message = "The field 'unitPrice' cannot be null")
    @Positive(message = "The unitPrice must be positive")
    private BigDecimal unitPrice;

    @NotNull(message = "The field 'productId' cannot be null")
    @Positive(message = "The productId must be positive")
    private Long productId;

    private String details;
}
