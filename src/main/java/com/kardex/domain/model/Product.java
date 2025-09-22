package com.kardex.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Domain model representing a product in the system
 * 
 */
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor 
public class Product {
    private Long id;

    private Long productId;

    private String reference;

    private String name;

    private String presentation;

    private String enterpriseId;

    private boolean state;

}
