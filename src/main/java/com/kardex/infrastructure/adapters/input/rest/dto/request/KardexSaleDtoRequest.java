package com.kardex.infrastructure.adapters.input.rest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KardexSaleDtoRequest {
    private Long quantity;

    private String details;

    private Long idProduct;
}
