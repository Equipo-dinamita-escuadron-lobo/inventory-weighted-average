package com.kardex.infrastructure.adapters.output.jpa.record;

import java.util.List;

import com.kardex.domain.model.Product;

public record ProductPartition(List<Product> newProducts, List<Product> existingProducts) {}
    
