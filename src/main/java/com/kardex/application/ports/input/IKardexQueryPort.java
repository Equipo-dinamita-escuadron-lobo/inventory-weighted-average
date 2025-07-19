package com.kardex.application.ports.input;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kardex.domain.model.Kardex;

public interface IKardexQueryPort {
    Page<Kardex>  findProductId(Long productId, Pageable pageable);
}
