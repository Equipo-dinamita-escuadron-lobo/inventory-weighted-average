package com.kardex.infrastructure.adapters.output.jpa.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;

public interface IProductRepository extends JpaRepository<ProductEntity, Long> {

    // Find all products by enterprise ID
    Collection<ProductEntity> findAllByEnterpriseId(String enterpriseId);

    // Find all products by ID
    List<ProductEntity> findByIdProductIn(List<Long> list);

    // Return a list of product IDs
    @Query("SELECT p.idProduct FROM ProductEntity p WHERE p.idProduct IN :ids")
    List<Long> findIdProductsByIdProductIn(@Param("ids") List<Long> ids);

    ProductEntity getReferenceByIdProduct(Long idProduct);

    boolean existsByIdProduct(Long id);

}
