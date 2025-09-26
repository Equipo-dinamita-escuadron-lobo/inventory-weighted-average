package com.kardex.infrastructure.adapters.input.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kardex.domain.port.IProductCommandRepositoryPort;
import com.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief REST controller for product command operations
 * 
 * Provides HTTP endpoints for product modification operations
 * including deletion with proper response formatting.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kardex/weighted-average")
@Slf4j
public class ProductCommandController {
    
    private final IProductCommandRepositoryPort productCommandPort;

    /**
     * @brief Deletes a specific product by ID and enterprise ID
     * @param productId Product identifier
     * @param enterpriseId Enterprise identifier
     * @return Response indicating deletion result
     */
    @DeleteMapping("/products/{enterpriseId}/{productId}")
    public ResponseEntity<ResponseDto<String>> deleteProduct(
            @PathVariable Long productId, 
            @PathVariable String enterpriseId) {
        
        try {
            log.info("Deleting product with ID {} for enterprise {}", productId, enterpriseId);
            String result = productCommandPort.deleteById(productId, enterpriseId);
            
            if (result.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseDto.<String>builder()
                        .data(result)
                        .status(404)
                        .message("Product not found")
                        .build());
            }
            
            return ResponseEntity.ok(ResponseDto.<String>builder()
                .data(result)
                .status(200)
                .message("Product deleted successfully")
                .build());
                
        } catch (Exception e) {
            log.error("Error deleting product with ID {} for enterprise {}: {}", productId, enterpriseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.<String>builder()
                    .data("Internal server error")
                    .status(500)
                    .message("Error occurred while deleting product")
                    .build());
        }
    }

    /**
     * @brief Deletes all products for a specific enterprise
     * @param enterpriseId Enterprise identifier
     * @return Response indicating deletion result
     */
    @DeleteMapping("/products/{enterpriseId}")
    public ResponseEntity<ResponseDto<String>> deleteAllProducts(@PathVariable String enterpriseId) {
        log.info("Deleting all products for enterprise {}", enterpriseId);
        String result = productCommandPort.deleteAllByEnterpriseId(enterpriseId);
        
        return ResponseEntity.ok(ResponseDto.<String>builder()
            .data(result)
            .status(200)
            .message("Products deleted successfully")
            .build());
    }
}
