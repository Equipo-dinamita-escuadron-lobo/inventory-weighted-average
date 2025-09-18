package com.kardex.domain.port;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kardex.domain.model.Kardex;
import com.kardex.domain.model.MovementType;

public interface IKardexQueryRepositoryPort {
    Page<Kardex> findProductId(Long productId, Pageable pageable);
    Page<Kardex> findProductIdAndDate(Long productId, Pageable pageable, LocalDate startDate, LocalDate endDate);
    List<Kardex> findByFactCodeAndProductIdAndType(String factCode, Long productId, MovementType type);
    Kardex getLatestKardexByProductId(Long productId);
    boolean existsByFactCodeAndProductIdAndType(String factCode, Long productId, MovementType type);
}
