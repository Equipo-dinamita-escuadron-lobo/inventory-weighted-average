package com.kardex.infrastructure.adapters.output.jpa.adapter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Function;

import org.springframework.stereotype.Repository;

import com.kardex.domain.model.Product;
import com.kardex.domain.port.IProductCommandRepositoryPort;
import com.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;
import com.kardex.infrastructure.adapters.output.jpa.mapper.IProductEntityCommandMapper;
import com.kardex.infrastructure.adapters.output.jpa.record.ProductPartition;
import com.kardex.infrastructure.adapters.output.jpa.repository.IProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductCommandAdapter implements IProductCommandRepositoryPort {

    private final IProductRepository productRepository;
    private final IProductEntityCommandMapper productEntityCommandMapper;

    @Override
    @Transactional
    public String saveAll(List<Product> products) {
        try {
            if (products.isEmpty()) {
                return "No products to process.";
            }

            // 1. Separate new products from existing ones
            ProductPartition partition = separateNewAndExistingProducts(products);

            // 2. Process both types of products
            int newCount = saveNewProducts(partition.newProducts());
            int updatedCount = updateExistingProducts(partition.existingProducts());

            // 3. Return result
            String message = String.format("Products processed: %d new, %d updated", newCount, updatedCount);
            log.info(message);
            return message;

        } catch (Exception e) {
            log.error("Error processing products", e);
            return "Error: " + e.getMessage();
        }
    }


    @Override
    public String save(Product product) {
        try {
            ProductEntity productEntity = productEntityCommandMapper.toEntity(product);
            productRepository.save(productEntity);
            return "Product saved successfully.";
        } catch (Exception e) {
            return "An error occurred while saving the product: " + e.getMessage();
        }
    }

    // Separate new products from existing ones
    private ProductPartition separateNewAndExistingProducts(List<Product> products) {
        List<Long> productIds = products.stream().map(Product::getIdProduct).toList();
        Set<Long> existingIds = new HashSet<>(productRepository.findIdProductsByIdProductIn(productIds));
        
        Map<Boolean, List<Product>> partitionedProducts = products.stream()
            .collect(Collectors.partitioningBy(p -> existingIds.contains(p.getIdProduct())));
        
        return new ProductPartition(
            partitionedProducts.get(false), 
            partitionedProducts.get(true)   
        );
    }

    // Save new products
    private int saveNewProducts(List<Product> newProducts) {
        if (newProducts.isEmpty()) {
            return 0;
        }

        List<ProductEntity> newEntities = productEntityCommandMapper.toEntity(newProducts);
        productRepository.saveAll(newEntities);
        
        log.info("Saved {} new products", newProducts.size());
        return newProducts.size();
    }

    // Update existing products
    private int updateExistingProducts(List<Product> existingProducts) {
        if (existingProducts.isEmpty()) {
            return 0;
        }

        List<Long> existingIds = existingProducts.stream().map(Product::getIdProduct).toList();
        List<ProductEntity> existingEntities = productRepository.findByIdProductIn(existingIds);
        
        Map<Long, Product> updateMap = existingProducts.stream()
            .collect(Collectors.toMap(Product::getIdProduct, Function.identity()));

        existingEntities.forEach(entity -> {
            Product product = updateMap.get(entity.getIdProduct());
            if (product != null) {
                productEntityCommandMapper.updateEntityFromProduct(product, entity);
            }     
            
        });
        
        productRepository.saveAll(existingEntities);
        log.info("Updated {} existing products", existingProducts.size());
        return existingProducts.size();
    }

}
