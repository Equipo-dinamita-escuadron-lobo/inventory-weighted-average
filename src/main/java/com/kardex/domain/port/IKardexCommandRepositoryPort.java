package com.kardex.domain.port;

import com.kardex.domain.model.Kardex;

public interface IKardexCommandRepositoryPort {
    Kardex registerPurchase(Kardex kardex);
    Kardex registerSale(Kardex kardex);
    Kardex getLatestKardexByProductId(Long productId);
}
