package com.kardex.domain.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Product {
    private Long id;

    private Long idProduct;

    private String reference;

    private String name;

    private String presentation;
    
    private String manager;

    private List<Kardex> kardexList;
}
