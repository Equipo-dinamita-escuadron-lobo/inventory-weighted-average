package com.kardex.infrastructure.adapters.input.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ProductDtoResponse {
    private Long id;

    private Long idProduct;

    private String reference;

    private String name;

    private String presentation;
    
    private String manager;

    private String enterpriseId;
}
