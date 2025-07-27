package com.kardex.domain.port;

import java.util.List;

import com.kardex.domain.model.Product;

public interface IProductCommandRepositoryPort {
     String saveAll(List<Product> products);
}
