package com.kardex.unit.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.kardex.application.ports.input.product.IProductCommandPort;
import com.kardex.infrastructure.adapters.input.rest.controller.ProductCommandController;
import com.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;

@ExtendWith(MockitoExtension.class)
public class ProductCommandControllerUnitTest {
    
    @Mock
    private IProductCommandPort productCommandPort;
    
    @InjectMocks
    private ProductCommandController productCommandController;
    
    private Long productId;
    private String enterpriseId;
    
    @BeforeEach
    void setUp() {
        productId = 100L;
        enterpriseId = "ENT-001";
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should delete product by ID successfully")
    void testDeleteProduct() {
        // Arrange
        String expectedResult = "Product deleted successfully";
        when(productCommandPort.deleteById(productId, enterpriseId)).thenReturn(expectedResult);
        
        // Act
        ResponseEntity<ResponseDto<String>> response = 
            productCommandController.deleteProduct(productId, enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Product deleted successfully", response.getBody().getMessage());
        assertEquals(expectedResult, response.getBody().getData());
        
        verify(productCommandPort).deleteById(productId, enterpriseId);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should delete all products for enterprise successfully")
    void testDeleteAllProducts() {
        // Arrange
        String expectedResult = "All products deleted successfully";
        when(productCommandPort.deleteAllByEnterpriseId(enterpriseId)).thenReturn(expectedResult);
        
        // Act
        ResponseEntity<ResponseDto<String>> response = 
            productCommandController.deleteAllProducts(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Products deleted successfully", response.getBody().getMessage());
        assertEquals(expectedResult, response.getBody().getData());
        
        verify(productCommandPort).deleteAllByEnterpriseId(enterpriseId);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle delete product with different enterprise IDs")
    void testDeleteProductWithDifferentEnterpriseIds() {
        // Arrange
        String enterpriseId1 = "ENT-001";
        String enterpriseId2 = "ENT-002";
        String result1 = "Product deleted for ENT-001";
        String result2 = "Product deleted for ENT-002";
        
        when(productCommandPort.deleteById(productId, enterpriseId1)).thenReturn(result1);
        when(productCommandPort.deleteById(productId, enterpriseId2)).thenReturn(result2);
        
        // Act
        ResponseEntity<ResponseDto<String>> response1 = 
            productCommandController.deleteProduct(productId, enterpriseId1);
        ResponseEntity<ResponseDto<String>> response2 = 
            productCommandController.deleteProduct(productId, enterpriseId2);
        
        // Assert
        assertEquals(result1, response1.getBody().getData());
        assertEquals(result2, response2.getBody().getData());
        
        verify(productCommandPort).deleteById(productId, enterpriseId1);
        verify(productCommandPort).deleteById(productId, enterpriseId2);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle delete product with different product IDs")
    void testDeleteProductWithDifferentProductIds() {
        // Arrange
        Long productId1 = 100L;
        Long productId2 = 200L;
        String result = "Product deleted";
        
        when(productCommandPort.deleteById(productId1, enterpriseId)).thenReturn(result);
        when(productCommandPort.deleteById(productId2, enterpriseId)).thenReturn(result);
        
        // Act
        ResponseEntity<ResponseDto<String>> response1 = 
            productCommandController.deleteProduct(productId1, enterpriseId);
        ResponseEntity<ResponseDto<String>> response2 = 
            productCommandController.deleteProduct(productId2, enterpriseId);
        
        // Assert
        assertNotNull(response1.getBody());
        assertNotNull(response2.getBody());
        assertEquals(200, response1.getBody().getStatus());
        assertEquals(200, response2.getBody().getStatus());
        
        verify(productCommandPort).deleteById(productId1, enterpriseId);
        verify(productCommandPort).deleteById(productId2, enterpriseId);
    }
    
    @Test
    @DisplayName("Should verify deleteById is called only once")
    void testDeleteProductVerifyOnce() {
        // Arrange
        String result = "Product deleted";
        when(productCommandPort.deleteById(productId, enterpriseId)).thenReturn(result);
        
        // Act
        productCommandController.deleteProduct(productId, enterpriseId);
        
        // Assert
        verify(productCommandPort, times(1)).deleteById(productId, enterpriseId);
        verifyNoMoreInteractions(productCommandPort);
    }
    
    @Test
    @DisplayName("Should verify deleteAllByEnterpriseId is called only once")
    void testDeleteAllProductsVerifyOnce() {
        // Arrange
        String result = "All products deleted";
        when(productCommandPort.deleteAllByEnterpriseId(enterpriseId)).thenReturn(result);
        
        // Act
        productCommandController.deleteAllProducts(enterpriseId);
        
        // Assert
        verify(productCommandPort, times(1)).deleteAllByEnterpriseId(enterpriseId);
        verifyNoMoreInteractions(productCommandPort);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should verify delete product response structure")
    void testDeleteProductResponseStructure() {
        // Arrange
        String result = "Product deleted";
        when(productCommandPort.deleteById(productId, enterpriseId)).thenReturn(result);
        
        // Act
        ResponseEntity<ResponseDto<String>> response = 
            productCommandController.deleteProduct(productId, enterpriseId);
        
        // Assert
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertNotNull(response.getBody().getMessage());
        assertTrue(response.getBody().getStatus() > 0);
        assertTrue(response.getBody().getData() instanceof String);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should verify delete all products response structure")
    void testDeleteAllProductsResponseStructure() {
        // Arrange
        String result = "All products deleted";
        when(productCommandPort.deleteAllByEnterpriseId(enterpriseId)).thenReturn(result);
        
        // Act
        ResponseEntity<ResponseDto<String>> response = 
            productCommandController.deleteAllProducts(enterpriseId);
        
        // Assert
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertNotNull(response.getBody().getMessage());
        assertTrue(response.getBody().getStatus() > 0);
        assertTrue(response.getBody().getData() instanceof String);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle delete product with long product ID")
    void testDeleteProductWithLongProductId() {
        // Arrange
        Long largeProductId = Long.MAX_VALUE;
        String result = "Product deleted";
        when(productCommandPort.deleteById(largeProductId, enterpriseId)).thenReturn(result);
        
        // Act
        ResponseEntity<ResponseDto<String>> response = 
            productCommandController.deleteProduct(largeProductId, enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getBody().getStatus());
        
        verify(productCommandPort).deleteById(largeProductId, enterpriseId);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle delete all products with special characters in enterprise ID")
    void testDeleteAllProductsWithSpecialEnterpriseId() {
        // Arrange
        String specialEnterpriseId = "ENT-001-ABC_123";
        String result = "Products deleted";
        when(productCommandPort.deleteAllByEnterpriseId(specialEnterpriseId)).thenReturn(result);
        
        // Act
        ResponseEntity<ResponseDto<String>> response = 
            productCommandController.deleteAllProducts(specialEnterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getBody().getStatus());
        
        verify(productCommandPort).deleteAllByEnterpriseId(specialEnterpriseId);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should return OK status for delete product")
    void testDeleteProductReturnsOkStatus() {
        // Arrange
        String result = "Product deleted";
        when(productCommandPort.deleteById(productId, enterpriseId)).thenReturn(result);
        
        // Act
        ResponseEntity<ResponseDto<String>> response = 
            productCommandController.deleteProduct(productId, enterpriseId);
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(200, response.getBody().getStatus());
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should return OK status for delete all products")
    void testDeleteAllProductsReturnsOkStatus() {
        // Arrange
        String result = "All products deleted";
        when(productCommandPort.deleteAllByEnterpriseId(enterpriseId)).thenReturn(result);
        
        // Act
        ResponseEntity<ResponseDto<String>> response = 
            productCommandController.deleteAllProducts(enterpriseId);
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(200, response.getBody().getStatus());
    }
}
