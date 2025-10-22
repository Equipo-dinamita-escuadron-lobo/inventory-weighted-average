package com.kardex.unit.application.kardex;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kardex.application.service.kardex.command.KardexValidationService;
import com.kardex.domain.model.Kardex;
import com.kardex.domain.model.MovementType;
import com.kardex.domain.port.common.IFormatterResultOutputPort;
import com.kardex.domain.port.common.IMessageServicePort;
import com.kardex.domain.port.kardex.IKardexQueryRepositoryPort;
import com.kardex.domain.port.product.IProductQueryRepositoryPort;

@ExtendWith(MockitoExtension.class)
public class KardexValidationServiceUnitTest {
    
    @Mock
    private IKardexQueryRepositoryPort kardexQueryRepositoryPort;
    
    @Mock
    private IFormatterResultOutputPort formatterResultOutputPort;
    
    @Mock
    private IProductQueryRepositoryPort productQueryRepositoryPort;
    
    @Mock
    private IMessageServicePort messageService;
    
    @InjectMocks
    private KardexValidationService kardexValidationService;
    
    private String factCode;
    private Long productId;
    private MovementType movementType;
    
    @BeforeEach
    void setUp() {
        factCode = "FACT001";
        productId = 1L;
        movementType = MovementType.PURCHASE;
    }
    
    @Test
    @DisplayName("Should validate business rules successfully when no duplicate exists")
    void testValidateBusinessRulesSuccess() {
        // Arrange
        when(kardexQueryRepositoryPort.existsByFactCodeAndProductIdAndType(factCode, productId, movementType))
            .thenReturn(false);
        
        // Act
        kardexValidationService.validateBusinessRules(factCode, productId, movementType);
        
        // Assert
        verify(kardexQueryRepositoryPort).existsByFactCodeAndProductIdAndType(factCode, productId, movementType);
        verify(formatterResultOutputPort, never()).returnBusinessRuleErrorResponse(anyInt(), anyString());
    }
    
    @Test
    @DisplayName("Should throw error when duplicate record exists")
    void testValidateBusinessRulesWithDuplicate() {
        // Arrange
        when(kardexQueryRepositoryPort.existsByFactCodeAndProductIdAndType(factCode, productId, movementType))
            .thenReturn(true);
        when(messageService.getMessage(anyString(), anyString())).thenReturn("Duplicate record error");
        
        // Act
        kardexValidationService.validateBusinessRules(factCode, productId, movementType);
        
        // Assert
        verify(kardexQueryRepositoryPort).existsByFactCodeAndProductIdAndType(factCode, productId, movementType);
        verify(formatterResultOutputPort).returnBusinessRuleErrorResponse(eq(400), anyString());
        verify(messageService).getMessage(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Should validate previous kardex exists successfully")
    void testValidatePreviousKardexExistsSuccess() {
        // Arrange
        Kardex lastKardex = new Kardex();
        lastKardex.setProductId(productId);
        String operation = "sale";
        
        // Act
        kardexValidationService.validatePreviousKardexExists(lastKardex, operation);
        
        // Assert - No exception should be thrown
        verify(formatterResultOutputPort, never()).returnEntityDoesNotExistErrorResponse(anyInt(), anyString());
    }
    
    @Test
    @DisplayName("Should throw error when previous kardex does not exist")
    void testValidatePreviousKardexExistsWithNull() {
        // Arrange
        Kardex lastKardex = null;
        String operation = "sale";
        when(messageService.getMessage(anyString(), anyString())).thenReturn("Missing record error");
        
        // Act
        kardexValidationService.validatePreviousKardexExists(lastKardex, operation);
        
        // Assert
        verify(formatterResultOutputPort).returnEntityDoesNotExistErrorResponse(eq(404), anyString());
        verify(messageService).getMessage(anyString(), eq(operation));
    }
    
    @Test
    @DisplayName("Should validate product exists successfully")
    void testValidateProductExistsSuccess() {
        // Arrange
        when(productQueryRepositoryPort.existsByProductId(productId)).thenReturn(true);
        
        // Act
        kardexValidationService.validateProductExists(productId);
        
        // Assert
        verify(productQueryRepositoryPort).existsByProductId(productId);
        verify(formatterResultOutputPort, never()).returnEntityDoesNotExistErrorResponse(anyInt(), anyString());
    }
    
    @Test
    @DisplayName("Should throw error when product does not exist")
    void testValidateProductExistsWithNonExistentProduct() {
        // Arrange
        when(productQueryRepositoryPort.existsByProductId(productId)).thenReturn(false);
        when(messageService.getMessage(anyString(), any(Object[].class))).thenReturn("Product not found");
        
        // Act
        kardexValidationService.validateProductExists(productId);
        
        // Assert
        verify(productQueryRepositoryPort).existsByProductId(productId);
        verify(formatterResultOutputPort).returnEntityDoesNotExistErrorResponse(eq(404), anyString());
    }
}