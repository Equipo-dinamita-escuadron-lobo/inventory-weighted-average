package com.kardex.infrastructure.adapters.output.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;

public interface IKardexRepository extends JpaRepository<KardexEntity, Long> {

    KardexEntity findTopByProductIdOrderByDateDesc(Long productId);

    
}
