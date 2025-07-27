package com.kardex.infrastructure.adapters.input.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class ProductSyncDto {
    private Long id;

    private String reference;

    private String name;

    private String enterpriseId;

    private boolean state;

}
