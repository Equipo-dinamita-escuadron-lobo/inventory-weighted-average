package com.kardex.infrastructure.adapters.output.jpa.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.kardex.domain.model.Product;
import com.kardex.domain.port.IProductCommandRepositoryPort;
import com.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;
import com.kardex.infrastructure.adapters.output.jpa.repository.IProductRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductCommandAdapter implements IProductCommandRepositoryPort {

    private final IProductRepository productRepository;

    @Override
    public String saveAll(List<Product> products) {
        try {
            List<ProductEntity> productEntities = products.stream()
                .map(product -> new ProductEntity(
                        null,
                        product.getIdProduct(),
                        product.getReference(),
                        product.getName(),
                        product.getPresentation(),
                        product.getManager(),
                        product.getEnterpriseId(),
                        null  // Assuming kardexList is not needed for saving
                    ))
                .toList();

            List<ProductEntity> savedEntities = productRepository.saveAll(productEntities);
            return "Successfully saved " + savedEntities.size() + " products.";
        } catch (Exception e) {
            return "An error occurred while saving products: " + e.getMessage();
        }
    }
    
}
