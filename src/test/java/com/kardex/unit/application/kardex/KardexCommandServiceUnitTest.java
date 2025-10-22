package com.kardex.unit.application.kardex;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kardex.application.service.kardex.command.KardexCommandService;
import com.kardex.application.service.kardex.command.KardexReturnService;
import com.kardex.application.service.kardex.command.KardexValidationService;
import com.kardex.application.service.kardex.command.StockIntegrationService;
import com.kardex.domain.model.Kardex;
import com.kardex.domain.model.MovementType;
import com.kardex.domain.model.Stock;
import com.kardex.domain.port.common.IFormatterResultOutputPort;
import com.kardex.domain.port.common.IMessageServicePort;
import com.kardex.domain.port.kardex.IKardexCommandRepositoryPort;
import com.kardex.domain.port.kardex.IKardexQueryRepositoryPort;

@ExtendWith(MockitoExtension.class)
public class KardexCommandServiceUnitTest {
    
    @Mock
    private IKardexCommandRepositoryPort kardexCommandRepositoryPort;
    
    @Mock
    private IKardexQueryRepositoryPort kardexQueryRepositoryPort;
    
    @Mock
    private IFormatterResultOutputPort formatterResultOutputPort;
    
    @Mock
    private IMessageServicePort messageService;
    
    @Mock
    private KardexValidationService validationService;
    
    @Mock
    private KardexReturnService returnService;
    
    @Mock
    private StockIntegrationService stockIntegrationService;
    
    @InjectMocks
    private KardexCommandService kardexCommandService;
    
    private Kardex kardex;
    private Kardex lastKardex;
    private Stock stock;
    
    @BeforeEach
    void setUp() {
        kardex = new Kardex();
        kardex.setProductId(1L);
        kardex.setQuantity(10);
        kardex.setUnitPrice(new BigDecimal("25.50"));
        kardex.setFactCode("FACT001");
        
        lastKardex = new Kardex();
        lastKardex.setProductId(1L);
        lastKardex.setBalanceQuantity(20);
        lastKardex.setBalanceUnitPrice(new BigDecimal("20.00"));
        lastKardex.setTotalBalance(new BigDecimal("400.00"));
        
        stock = Stock.builder()
            .productId(1L)
            .quantity(10)
            .price(new BigDecimal("25.50"))
            .build();
    }
    
    @Test
    @DisplayName("Should register purchase successfully when no previous kardex exists")
    void testRegisterPurchaseWithNoPreviousKardex() {
        // Arrange
        when(kardexQueryRepositoryPort.getLatestKardexByProductId(1L)).thenReturn(null);
        when(stockIntegrationService.createStock(any(Kardex.class))).thenReturn(stock);
        when(kardexCommandRepositoryPort.registerPurchase(any(Kardex.class))).thenReturn(kardex);
       
        // Act
        Kardex result = kardexCommandService.registerPurchase(kardex);
        
        // Assert
        assertNotNull(result);
        verify(validationService).validateProductExists(1L);
        verify(validationService).validateBusinessRules("FACT001", 1L, MovementType.PURCHASE);
        verify(stockIntegrationService).callApiStockService(stock, true);
        verify(kardexCommandRepositoryPort).registerPurchase(kardex);
        assertEquals(MovementType.PURCHASE, kardex.getType());
    }
    
    @Test
    @DisplayName("Should register purchase successfully when previous kardex exists")
    void testRegisterPurchaseWithPreviousKardex() {
        // Arrange
        when(kardexQueryRepositoryPort.getLatestKardexByProductId(1L)).thenReturn(lastKardex);
        when(stockIntegrationService.createStock(any(Kardex.class))).thenReturn(stock);
        when(kardexCommandRepositoryPort.registerPurchase(any(Kardex.class))).thenReturn(kardex);
        
        // Act
        Kardex result = kardexCommandService.registerPurchase(kardex);
        
        // Assert
        assertNotNull(result);
        verify(validationService).validateProductExists(1L);
        verify(validationService).validateBusinessRules("FACT001", 1L, MovementType.PURCHASE);
        verify(stockIntegrationService).callApiStockService(stock, true);
        verify(kardexCommandRepositoryPort).registerPurchase(kardex);
        assertEquals(MovementType.PURCHASE, kardex.getType());
    }
    
    @Test
    @DisplayName("Should register sale successfully")
    void testRegisterSale() {
        // Arrange
        when(kardexQueryRepositoryPort.getLatestKardexByProductId(1L)).thenReturn(lastKardex);
        when(stockIntegrationService.createStock(any(Kardex.class))).thenReturn(stock);
        when(kardexCommandRepositoryPort.registerSale(any(Kardex.class))).thenReturn(kardex);
        
        // Act
        Kardex result = kardexCommandService.registerSale(kardex);
        
        // Assert
        assertNotNull(result);
        verify(validationService).validateProductExists(1L);
        verify(validationService).validateBusinessRules("FACT001", 1L, MovementType.SALE);
        verify(validationService).validatePreviousKardexExists(lastKardex, "sale");
        verify(stockIntegrationService).callApiStockService(stock, false);
        verify(kardexCommandRepositoryPort).registerSale(kardex);
        assertEquals(MovementType.SALE, kardex.getType());
    }
    
    @Test
    @DisplayName("Should register purchase return successfully")
    void testRegisterReturnOnPurchase() {
        // Arrange
        BigDecimal originalPrice = new BigDecimal("25.50");
        when(returnService.getUnitPriceIfReturnAllowed("FACT001", 10, 1L, MovementType.PURCHASE))
            .thenReturn(originalPrice);
        when(kardexQueryRepositoryPort.getLatestKardexByProductId(1L)).thenReturn(lastKardex);
        when(stockIntegrationService.createStock(any(Kardex.class))).thenReturn(stock);
        when(kardexCommandRepositoryPort.registerReturnOnPurchase(any(Kardex.class))).thenReturn(kardex);
        
        // Act
        Kardex result = kardexCommandService.registerReturnOnPurchase(kardex);
        
        // Assert
        assertNotNull(result);
        verify(validationService).validateProductExists(1L);
        verify(validationService).validatePreviousKardexExists(lastKardex, "purchase return");
        verify(returnService).getUnitPriceIfReturnAllowed("FACT001", 10, 1L, MovementType.PURCHASE);
        verify(stockIntegrationService).callApiStockService(stock, false);
        verify(kardexCommandRepositoryPort).registerReturnOnPurchase(kardex);
        assertEquals(MovementType.PURCHASERETURN, kardex.getType());
        assertEquals(originalPrice, kardex.getUnitPrice());
    }
    
    @Test
    @DisplayName("Should register sale return successfully")
    void testRegisterReturnOnSale() {
        // Arrange
        BigDecimal originalPrice = new BigDecimal("25.50");
        when(returnService.getUnitPriceIfReturnAllowed("FACT001", 10, 1L, MovementType.SALE))
            .thenReturn(originalPrice);
        when(kardexQueryRepositoryPort.getLatestKardexByProductId(1L)).thenReturn(lastKardex);
        when(stockIntegrationService.createStock(any(Kardex.class))).thenReturn(stock);
        when(kardexCommandRepositoryPort.registerReturnOnSale(any(Kardex.class))).thenReturn(kardex);
        
        // Act
        Kardex result = kardexCommandService.registerReturnOnSale(kardex);
        
        // Assert
        assertNotNull(result);
        verify(validationService).validateProductExists(1L);
        verify(validationService).validatePreviousKardexExists(lastKardex, "sale return");
        verify(returnService).getUnitPriceIfReturnAllowed("FACT001", 10, 1L, MovementType.SALE);
        verify(stockIntegrationService).callApiStockService(stock, true);
        verify(kardexCommandRepositoryPort).registerReturnOnSale(kardex);
        assertEquals(MovementType.SALESRETURN, kardex.getType());
        assertEquals(originalPrice, kardex.getUnitPrice());
    }
    
    @Test
    @DisplayName("Should throw error when balance unit price is zero")
    void testRegisterPurchaseWithZeroBalanceUnitPrice() {
        // Arrange
        kardex.setUnitPrice(BigDecimal.ZERO);
        when(kardexQueryRepositoryPort.getLatestKardexByProductId(1L)).thenReturn(null);
        when(messageService.getMessage(any(), anyString())).thenReturn("Invalid value for balance unit price");
        doThrow(new RuntimeException("Business rule error")).when(formatterResultOutputPort)
            .returnBusinessRuleErrorResponse(anyInt(), anyString());
        
        // Act & Assert - Should throw exception when balance unit price is zero
        assertThrows(RuntimeException.class, () -> {
            kardexCommandService.registerPurchase(kardex);
        });
        
        // Verify that error response was called
        verify(formatterResultOutputPort).returnBusinessRuleErrorResponse(
            eq(400), 
            anyString()
        );
    }
    
    @Test
    @DisplayName("Should delete all kardex records successfully")
    void testDeleteAllSuccess() {
        // Arrange
        doNothing().when(kardexCommandRepositoryPort).deleteAll();
        
        // Act
        kardexCommandService.deleteAll();
        
        // Assert
        verify(kardexCommandRepositoryPort).deleteAll();
    }
    
    @Test
    @DisplayName("Should handle exception when deleting all kardex records")
    void testDeleteAllWithException() {
        // Arrange
        doThrow(new RuntimeException("Database error")).when(kardexCommandRepositoryPort).deleteAll();
        
        // Act & Assert - Should not throw exception, just log error
        assertDoesNotThrow(() -> kardexCommandService.deleteAll());
        
        // Verify that deleteAll was called
        verify(kardexCommandRepositoryPort).deleteAll();
    }
}
