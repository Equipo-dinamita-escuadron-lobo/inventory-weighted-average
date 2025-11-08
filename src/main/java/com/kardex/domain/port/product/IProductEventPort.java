package com.kardex.domain.port.product;

public interface IProductEventPort {
    void publishCreatedProductEvent(boolean isItBeingUsed);

}
