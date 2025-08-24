package com.kardex.domain.port;

import java.time.Instant;
import java.util.List;

import com.kardex.domain.model.Product;

public interface IProductClientPort {
    List<Product> findAllProductsByEnterpriseId(String enterpriseId, Instant since);
}
