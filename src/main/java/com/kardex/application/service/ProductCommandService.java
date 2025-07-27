package com.kardex.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kardex.application.ports.input.IProductCommandPort;
import com.kardex.application.ports.output.IProductClient;
import com.kardex.domain.model.Product;
import com.kardex.domain.port.IFormatterResultOutputPort;
import com.kardex.domain.port.IProductCommandRepositoryPort;
import com.kardex.infrastructure.adapters.input.rest.dto.response.ProductSyncDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductCommandService implements IProductCommandPort {

    private final IProductCommandRepositoryPort productCommandRepositoryPort;
    private final IProductClient productClient;

    @Override
    public void test2(String message) {
        try {
            List<ProductSyncDto> products = productClient.findAllProductsByEnterpriseId("1");
            if(products != null && !products.isEmpty()) {
                System.out.println("Products found: " + products.size());
            } else {
                System.out.println("No products found for the given enterprise ID.");
            }
        } catch (Exception e) {
            System.out.println("An error occurred while fetching products: " + e.getMessage());
        }
    }

    @Override
    public String saveAll(String enterpriseId) {
        try {
            List<ProductSyncDto> products = productClient.findAllProductsByEnterpriseId(enterpriseId);
            List<Product> productList = products.stream()
                .map(product -> new Product(
                        null, 
                        product.getId(),
                        product.getReference(), 
                        product.getName(), 
                        "presentation",
                        "manager",
                        product.getEnterpriseId(),
                        null
                    ))
                .toList();


            if (products != null && !products.isEmpty()) {
                String result = productCommandRepositoryPort.saveAll(productList);
                return result;
            } else {
                return "No products found for the given enterprise ID.";
            }
            
        } catch (Exception e) {
            return "An error occurred while saving products: " + e.getMessage();
        }
    }
    
}
