package com.kardex.domain.port;

import com.kardex.domain.model.Stock;

public interface IStockClientPort {
    void buyStock(Stock stock);
}
