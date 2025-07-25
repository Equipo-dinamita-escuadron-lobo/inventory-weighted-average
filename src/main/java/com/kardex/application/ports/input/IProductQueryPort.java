package com.kardex.application.ports.input;

import java.util.List;

import com.kardex.domain.model.Product;

public interface IProductQueryPort {
    List<Product> findAll(String enterpriseId);
}
