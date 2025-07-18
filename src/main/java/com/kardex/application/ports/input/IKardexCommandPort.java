package com.kardex.application.ports.input;

import com.kardex.domain.model.Kardex;

public interface IKardexCommandPort {
    Kardex registerPurchase(Kardex kardex);
    Kardex registerSale(Kardex kardex);
    void test(String message);
}
