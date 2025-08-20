package com.kardex.infrastructure.adapters.output.jpa.repository;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;

public interface IKardexRepository extends JpaRepository<KardexEntity, Long> {

    // Find the latest Kardex entry by product ID
    KardexEntity findTopByIdProductOrderByDateDesc(Long idProduct);

    // Find all Kardex entries by product ID
    Page<KardexEntity> findByIdProduct(Long idProduct, Pageable pageable);

    // Find all Kardex entries by product ID and date range
    Page<KardexEntity> findByIdProductAndDateBetween(
        Long idProduct, 
        ZonedDateTime startDate, 
        ZonedDateTime endDate,
        Pageable pageable
    );

    // Find all Kardex entries by fact code and product ID
    List<KardexEntity> findByFactCodeAndIdProduct(Long factCode, Long idProduct);

}
