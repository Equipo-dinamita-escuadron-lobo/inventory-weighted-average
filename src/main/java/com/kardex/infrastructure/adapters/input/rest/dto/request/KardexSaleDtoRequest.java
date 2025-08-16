package com.kardex.infrastructure.adapters.input.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KardexSaleDtoRequest {
    @NotNull(message = "The field 'quantity' cannot be null")
    @Positive(message = "The quantity must be positive") 
    private Long quantity;

    @NotNull(message = "The field 'factCode' cannot be null")
    @Positive(message = "The factCode must be positive")
    private Long factCode;
    
    @NotNull(message = "The field 'idProduct' cannot be null")
    @Positive(message = "The idProduct must be positive")
    private Long idProduct;

    private String details;
}
