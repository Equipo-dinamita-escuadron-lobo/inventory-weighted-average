package com.kardex.domain.port;

import java.util.List;

import com.kardex.domain.model.Product;

public interface IProductQueryRepositoryPort {
    List<Product> findAll(String enterpriseId);
}
