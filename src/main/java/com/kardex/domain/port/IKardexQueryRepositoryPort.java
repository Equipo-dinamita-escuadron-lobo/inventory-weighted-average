package com.kardex.domain.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kardex.domain.model.Kardex;

public interface IKardexQueryRepositoryPort {
    Page<Kardex> findProductId(Long productId, Pageable pageable);
}
