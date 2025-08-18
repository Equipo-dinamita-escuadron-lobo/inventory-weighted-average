package com.kardex.unit.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.kardex.domain.model.Kardex;
import com.kardex.domain.model.MovementType;

public class KardexTest {
    
    private Kardex kardex;
    
    @BeforeEach
    void setUp() {
        kardex = new Kardex();
    }
    
    @Test
    @DisplayName("Should add the current date to the kardex")
    void testAddDate() {
        // Arrange
        ZonedDateTime before = ZonedDateTime.now(ZoneId.of("America/Bogota")).truncatedTo(ChronoUnit.SECONDS);
        
        // Act
        kardex.addDate();
        
        // Assert
        ZonedDateTime after = ZonedDateTime.now(ZoneId.of("America/Bogota")).truncatedTo(ChronoUnit.SECONDS);
        assertNotNull(kardex.getDate());
        ZonedDateTime kardexDate = kardex.getDate().truncatedTo(ChronoUnit.SECONDS);
        assertTrue(kardexDate.equals(before) || kardexDate.isAfter(before) && kardexDate.isBefore(after) 
                || kardexDate.equals(after));
    }
    
    @Test
    @DisplayName("Should correctly calculate the balance when adding a purchase")
    void testAddPurchase() {
        // Arrange
        kardex.setQuantity(10L);
        kardex.setUnitPrice(new BigDecimal("25.50"));
        kardex.setType(MovementType.PURCHASE);
        Long lastQuantity = 5L;
        BigDecimal lastTotalBalance = new BigDecimal("100.00");
        
        // Act
        kardex.addPurchase(lastQuantity, lastTotalBalance);
        
        // Assert
        assertEquals(15L, kardex.getBalanceQuantity());
        BigDecimal expectedTotal = new BigDecimal("355.00"); // 100 + (10 * 25.50)
        assertEquals(expectedTotal, kardex.getTotalBalance());
        
        // The average unit price should be the total divided by the quantity
        BigDecimal expectedUnitPrice = expectedTotal.divide(new BigDecimal("15"), 2, RoundingMode.HALF_UP);
        assertEquals(expectedUnitPrice, kardex.getBalanceUnitPrice());
    }
    
    @Test
    @DisplayName("Should handle a purchase with zero balance quantity correctly")
    void testAddPurchaseWithZeroBalanceQuantity() {
        // Arrange
        kardex.setQuantity(10L);
        kardex.setUnitPrice(new BigDecimal("25.50"));
        Long lastQuantity = -10L; // To make the resulting balance zero
        BigDecimal lastTotalBalance = new BigDecimal("100.00");
        
        // Act
        kardex.addPurchase(lastQuantity, lastTotalBalance);
        
        // Assert
        assertEquals(0L, kardex.getBalanceQuantity());
        assertEquals(BigDecimal.ZERO, kardex.getBalanceUnitPrice());
    }
    
    @Test
    @DisplayName("Should correctly calculate the balance when adding a sale")
    void testAddSale() {
        // Arrange
        kardex.setQuantity(5L);
        kardex.setType(MovementType.SALE);
        Long lastQuantity = 20L;
        BigDecimal lastUnitPrice = new BigDecimal("15.00");
        BigDecimal lastTotalBalance = new BigDecimal("300.00");
        
        // Act
        kardex.addSale(lastQuantity, lastUnitPrice, lastTotalBalance);
        
        // Assert
        assertEquals(15L, kardex.getBalanceQuantity());
        assertEquals(lastUnitPrice, kardex.getBalanceUnitPrice());
        assertEquals(lastUnitPrice, kardex.getUnitPrice());
        
        BigDecimal expectedTotal = new BigDecimal("225.00"); // 300 - (5 * 15)
        assertEquals(expectedTotal, kardex.getTotalBalance());
    }
    
    @Test
    @DisplayName("Should handle a sale with negative balance correctly")
    void testAddSaleWithNegativeBalance() {
        // Arrange
        kardex.setQuantity(25L);
        Long lastQuantity = 20L; // To make the resulting balance negative
        BigDecimal lastUnitPrice = new BigDecimal("15.00");
        BigDecimal lastTotalBalance = new BigDecimal("300.00");
        
        // Act
        kardex.addSale(lastQuantity, lastUnitPrice, lastTotalBalance);
        
        // Assert
        assertEquals(-5L, kardex.getBalanceQuantity());
        assertEquals(BigDecimal.ZERO, kardex.getBalanceUnitPrice());
    }
    
    @Test
    @DisplayName("Should handle a sale with zero balance correctly")
    void testAddSaleWithZeroBalance() {
        // Arrange
        kardex.setQuantity(20L);
        Long lastQuantity = 20L; // To make the resulting balance zero
        BigDecimal lastUnitPrice = new BigDecimal("15.00");
        BigDecimal lastTotalBalance = new BigDecimal("300.00");
        
        // Act
        kardex.addSale(lastQuantity, lastUnitPrice, lastTotalBalance);
        
        // Assert
        assertEquals(0L, kardex.getBalanceQuantity());
        assertEquals(BigDecimal.ZERO, kardex.getBalanceUnitPrice());
        assertEquals(BigDecimal.ZERO, kardex.getTotalBalance());
    }
    
    @Test
    @DisplayName("Should correctly calculate the balance when returning a sale")
    void testReturnOnSale() {
        // Arrange
        kardex.setQuantity(5L);
        kardex.setType(MovementType.SALESRETURN);
        Long lastQuantity = 15L;
        BigDecimal lastUnitPrice = new BigDecimal("20.00");
        BigDecimal lastTotalBalance = new BigDecimal("300.00");
        
        // Act
        kardex.returnOnSale(lastQuantity, lastUnitPrice, lastTotalBalance);
        
        // Assert
        assertEquals(20L, kardex.getBalanceQuantity());
        BigDecimal expectedTotal = new BigDecimal("400.00"); // 300 + (5 * 20)
        assertEquals(expectedTotal, kardex.getTotalBalance());
        
        BigDecimal expectedUnitPrice = expectedTotal.divide(new BigDecimal("20"), 2, RoundingMode.HALF_UP);
        assertEquals(expectedUnitPrice, kardex.getBalanceUnitPrice());
    }
    
    @Test
    @DisplayName("Should handle a sale return with zero balance correctly")
    void testReturnOnSaleWithZeroBalance() {
        // Arrange
        kardex.setQuantity(5L);
        Long lastQuantity = -5L; // To make the resulting balance zero
        BigDecimal lastUnitPrice = new BigDecimal("20.00");
        BigDecimal lastTotalBalance = new BigDecimal("0.00");
        
        // Act
        kardex.returnOnSale(lastQuantity, lastUnitPrice, lastTotalBalance);
        
        // Assert
        assertEquals(0L, kardex.getBalanceQuantity());
        assertEquals(BigDecimal.ZERO, kardex.getBalanceUnitPrice());
    }
    
    @Test
    @DisplayName("Should correctly calculate the balance when returning a purchase")
    void testReturnOnPurchase() {
        // Arrange
        kardex.setQuantity(5L);
        kardex.setUnitPrice(new BigDecimal("20.00"));
        kardex.setType(MovementType.PURCHASERETURN);
        Long lastQuantity = 25L;
        BigDecimal lastTotalBalance = new BigDecimal("500.00");
        
        // Act
        kardex.returnOnPurchase(lastQuantity, lastTotalBalance);
        
        // Assert
        assertEquals(20L, kardex.getBalanceQuantity()); // 25 - 5
        BigDecimal expectedTotal = new BigDecimal("400.00"); // 500 - (5 * 20)
        assertEquals(expectedTotal, kardex.getTotalBalance());
        
        BigDecimal expectedUnitPrice = expectedTotal.divide(new BigDecimal("20"), 2, RoundingMode.HALF_UP); 
        assertEquals(expectedUnitPrice, kardex.getBalanceUnitPrice());
    }
    
    @Test
    @DisplayName("Should handle a purchase return with negative balance correctly")
    void testReturnOnPurchaseWithNegativeBalance() {
        // Arrange
        kardex.setQuantity(30L);
        kardex.setUnitPrice(new BigDecimal("20.00"));
        Long lastQuantity = 25L; // To make the resulting balance negative
        BigDecimal lastTotalBalance = new BigDecimal("500.00");
        
        // Act
        kardex.returnOnPurchase(lastQuantity, lastTotalBalance);
        
        // Assert
        assertEquals(-5L, kardex.getBalanceQuantity());
        assertEquals(BigDecimal.ZERO, kardex.getBalanceUnitPrice());
    }
    
    @Test
    @DisplayName("Should handle a purchase return with zero balance correctly")
    void testReturnOnPurchaseWithZeroBalance() {
        // Arrange
        kardex.setQuantity(25L);
        kardex.setUnitPrice(new BigDecimal("20.00"));
        Long lastQuantity = 25L; // To make the resulting balance zero
        BigDecimal lastTotalBalance = new BigDecimal("500.00");
        
        // Act
        kardex.returnOnPurchase(lastQuantity, lastTotalBalance);
        
        // Assert
        assertEquals(0L, kardex.getBalanceQuantity());
        assertEquals(BigDecimal.ZERO, kardex.getBalanceUnitPrice());
        assertEquals(BigDecimal.ZERO, kardex.getTotalBalance());
    }
    
    @Test
    @DisplayName("Should reset balances when calling resetBalancesIfZero")
    void testResetBalancesIfZero() {
        // Arrange
        kardex.setBalanceQuantity(10L);
        kardex.setBalanceUnitPrice(new BigDecimal("25.00"));
        kardex.setTotalBalance(new BigDecimal("250.00"));
        
        // Act
        kardex.resetBalancesIfZero();
        
        // Assert
        assertEquals(0L, kardex.getBalanceQuantity());
        assertEquals(BigDecimal.ZERO, kardex.getBalanceUnitPrice());
        assertEquals(BigDecimal.ZERO, kardex.getTotalBalance());
    }

    @Test
    @DisplayName("Should update details if not null")
    void testUpdateDetailIfNotNull() {
        // Arrange
        kardex.setType(MovementType.SALE);
        kardex.setFactCode(500L);
        kardex.setDetails("Venta - Factura: 500");

        // Act
        kardex.updateDetailIfNotNull();

        // Assert
        assertEquals("Venta - Factura: 500", kardex.getDetails());
    }

    @Test
    @DisplayName("Should update details if null")
    void testUpdateDetailIfNull() {
        // Arrange
        kardex.setType(MovementType.SALE);
        kardex.setFactCode(500L);
        kardex.setDetails(null);

        // Act
        kardex.updateDetailIfNotNull();

        // Assert
        assertEquals("Venta - Factura: 500", kardex.getDetails());
    }
}   
