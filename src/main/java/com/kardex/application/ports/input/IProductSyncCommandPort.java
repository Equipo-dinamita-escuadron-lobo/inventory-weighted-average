package com.kardex.application.ports.input;

public interface IProductSyncCommandPort {
    String syncProductsByEnterpriseId(String enterpriseId);
}
