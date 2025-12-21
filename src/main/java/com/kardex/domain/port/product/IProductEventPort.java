package com.kardex.domain.port.product;

public interface IProductEventPort {
    void publishUsedProductEvent(Long productId, Integer quantityUsed);

}
