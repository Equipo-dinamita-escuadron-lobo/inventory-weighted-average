package com.kardex.domain.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor 
public class Product {
    private Long id;

    private Long idProduct;

    private String reference;

    private String name;

    private String presentation;
    
    private String manager;

    private String enterpriseId;

    private boolean state;

    private List<Kardex> kardexList;
}
