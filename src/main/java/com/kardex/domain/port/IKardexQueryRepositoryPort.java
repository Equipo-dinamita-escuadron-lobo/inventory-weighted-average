package com.kardex.domain.port;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kardex.domain.model.Kardex;

public interface IKardexQueryRepositoryPort {
    Page<Kardex> findProductId(Long productId, Pageable pageable);
    Page<Kardex> findProductIdAndDate(Long productId, Pageable pageable, LocalDate startDate, LocalDate endDate);
    List<Kardex> findByFactCodeAndProductId(Long factCode, Long productId);
    Kardex getLatestKardexByProductId(Long productId);
}
