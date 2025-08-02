package com.kardex.infrastructure.adapters.output.jpa.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;

public interface IProductRepository extends JpaRepository<ProductEntity, Long> {

    Collection<ProductEntity> findAllByEnterpriseId(String enterpriseId);

    List<ProductEntity> findByIdProductIn(List<Long> list);
    
    @Query("SELECT p.idProduct FROM ProductEntity p WHERE p.idProduct IN :ids")
    List<Long> findIdProductsByIdProductIn(@Param("ids") List<Long> ids);

}
