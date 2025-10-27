package com.kardex.infrastructure.adapters.input.rest.dto.request;

import com.kardex.domain.model.MovementType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for receiving sale operations in the Kardex system
 * Contains fields necessary for registering a product sale
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KardexSaleDtoRequest {
    @NotNull(message = "{kardex.validation.quantity.notnull}")
    @Positive(message = "{kardex.validation.quantity.positive}")
    private Long quantity;

    @NotNull(message = "{kardex.validation.productid.notnull}")
    @Positive(message = "{kardex.validation.productid.positive}")
    private Long productId;

    private String details;

    private final MovementType type = MovementType.ADJUSTMENTEXIT;
}
