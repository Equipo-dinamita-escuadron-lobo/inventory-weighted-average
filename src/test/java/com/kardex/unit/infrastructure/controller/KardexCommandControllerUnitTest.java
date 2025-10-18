package com.kardex.unit.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.kardex.application.ports.input.kardex.IKardexCommandPort;
import com.kardex.domain.model.Kardex;
import com.kardex.domain.model.MovementType;
import com.kardex.infrastructure.adapters.input.rest.controller.KardexCommandController;
import com.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import com.kardex.infrastructure.adapters.input.rest.dto.request.KardexPurchaseDtoRequest;
import com.kardex.infrastructure.adapters.input.rest.dto.request.KardexSaleDtoRequest;
import com.kardex.infrastructure.adapters.input.rest.dto.response.KardexDtoResponse;
import com.kardex.infrastructure.adapters.input.rest.mapper.IKardexResponseMapper;
import com.kardex.infrastructure.adapters.input.rest.mapper.IKardexRestMapper;

@ExtendWith(MockitoExtension.class)
public class KardexCommandControllerUnitTest {
    
    @Mock
    private IKardexCommandPort kardexCommandPort;
    
    @Mock
    private IKardexRestMapper kardexRestMapper;
    
    @Mock
    private IKardexResponseMapper kardexResponseMapper;
    
    @InjectMocks
    private KardexCommandController kardexCommandController;
    
    private KardexPurchaseDtoRequest purchaseRequest;
    private KardexSaleDtoRequest saleRequest;
    private Kardex purchaseKardex;
    private Kardex saleKardex;
    private KardexDtoResponse purchaseResponse;
    private KardexDtoResponse saleResponse;
    
    @BeforeEach
    void setUp() {
        // Setup purchase request
        purchaseRequest = new KardexPurchaseDtoRequest();
        purchaseRequest.setProductId(100L);
        purchaseRequest.setQuantity(50L);
        purchaseRequest.setUnitPrice(new BigDecimal("100.00"));
        purchaseRequest.setDetails("Purchase of office supplies");
        
        // Setup sale request
        saleRequest = new KardexSaleDtoRequest();
        saleRequest.setProductId(100L);
        saleRequest.setQuantity(20L);
        saleRequest.setDetails("Sale to client ABC");
        
        // Setup purchase kardex domain
        purchaseKardex = new Kardex();
        purchaseKardex.setId(1L);
        purchaseKardex.setProductId(100L);
        purchaseKardex.setQuantity(50);
        purchaseKardex.setUnitPrice(new BigDecimal("100.00"));
        purchaseKardex.setType(MovementType.PURCHASE);
        purchaseKardex.setDate(ZonedDateTime.now());
        purchaseKardex.setDetails("Purchase of office supplies");
        
        // Setup sale kardex domain
        saleKardex = new Kardex();
        saleKardex.setId(2L);
        saleKardex.setProductId(100L);
        saleKardex.setQuantity(20);
        saleKardex.setUnitPrice(new BigDecimal("100.00"));
        saleKardex.setType(MovementType.SALE);
        saleKardex.setDate(ZonedDateTime.now());
        saleKardex.setDetails("Sale to client ABC");
        
        // Setup purchase response
        purchaseResponse = new KardexDtoResponse();
        purchaseResponse.setId(1L);
        purchaseResponse.setQuantity(50L);
        purchaseResponse.setType(MovementType.PURCHASE);
        
        // Setup sale response
        saleResponse = new KardexDtoResponse();
        saleResponse.setId(2L);
        saleResponse.setQuantity(20L);
        saleResponse.setType(MovementType.SALE);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should register purchase successfully")
    void testPurchaseKardex() {
        // Arrange
        when(kardexRestMapper.toDomain(purchaseRequest)).thenReturn(purchaseKardex);
        when(kardexCommandPort.registerPurchase(purchaseKardex)).thenReturn(purchaseKardex);
        when(kardexResponseMapper.toDtoResponse(purchaseKardex)).thenReturn(purchaseResponse);
        
        // Act
        ResponseEntity<ResponseDto<KardexDtoResponse>> response = kardexCommandController.purchaseKardex(purchaseRequest);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Kardex purchase registered successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(1L, response.getBody().getData().getId());
        assertEquals(50L, response.getBody().getData().getQuantity());
        assertEquals(MovementType.PURCHASE, response.getBody().getData().getType());
        
        verify(kardexRestMapper).toDomain(purchaseRequest);
        verify(kardexCommandPort).registerPurchase(purchaseKardex);
        verify(kardexResponseMapper).toDtoResponse(purchaseKardex);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should register sale successfully")
    void testSaleKardex() {
        // Arrange
        when(kardexRestMapper.toDomain(saleRequest)).thenReturn(saleKardex);
        when(kardexCommandPort.registerSale(saleKardex)).thenReturn(saleKardex);
        when(kardexResponseMapper.toDtoResponse(saleKardex)).thenReturn(saleResponse);
        
        // Act
        ResponseEntity<ResponseDto<KardexDtoResponse>> response = kardexCommandController.saleKardex(saleRequest);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Kardex sale registered successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(2L, response.getBody().getData().getId());
        assertEquals(20L, response.getBody().getData().getQuantity());
        assertEquals(MovementType.SALE, response.getBody().getData().getType());
        
        verify(kardexRestMapper).toDomain(saleRequest);
        verify(kardexCommandPort).registerSale(saleKardex);
        verify(kardexResponseMapper).toDtoResponse(saleKardex);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should delete all kardex records successfully")
    void testDeleteAllKardex() {
        // Arrange
        doNothing().when(kardexCommandPort).deleteAll();
        
        // Act
        ResponseEntity<ResponseDto<Void>> response = kardexCommandController.deleteAllKardex();
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("All kardex records deleted successfully", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        
        verify(kardexCommandPort).deleteAll();
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle purchase with minimum valid values")
    void testPurchaseKardexWithMinimumValues() {
        // Arrange
        KardexPurchaseDtoRequest minRequest = new KardexPurchaseDtoRequest();
        minRequest.setProductId(1L);
        minRequest.setQuantity(1L);
        minRequest.setUnitPrice(new BigDecimal("0.01"));
        
        Kardex minKardex = new Kardex();
        minKardex.setId(3L);
        minKardex.setProductId(1L);
        minKardex.setQuantity(1);
        minKardex.setUnitPrice(new BigDecimal("0.01"));
        minKardex.setType(MovementType.PURCHASE);
        
        KardexDtoResponse minResponse = new KardexDtoResponse();
        minResponse.setId(3L);
        minResponse.setQuantity(1L);
        
        when(kardexRestMapper.toDomain(minRequest)).thenReturn(minKardex);
        when(kardexCommandPort.registerPurchase(minKardex)).thenReturn(minKardex);
        when(kardexResponseMapper.toDtoResponse(minKardex)).thenReturn(minResponse);
        
        // Act
        ResponseEntity<ResponseDto<KardexDtoResponse>> response = kardexCommandController.purchaseKardex(minRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getBody().getStatus());
        assertEquals(1L, response.getBody().getData().getQuantity());
        
        verify(kardexCommandPort).registerPurchase(minKardex);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle sale with minimum valid values")
    void testSaleKardexWithMinimumValues() {
        // Arrange
        KardexSaleDtoRequest minRequest = new KardexSaleDtoRequest();
        minRequest.setProductId(1L);
        minRequest.setQuantity(1L);
        
        Kardex minKardex = new Kardex();
        minKardex.setId(4L);
        minKardex.setProductId(1L);
        minKardex.setQuantity(1);
        minKardex.setType(MovementType.SALE);
        
        KardexDtoResponse minResponse = new KardexDtoResponse();
        minResponse.setId(4L);
        minResponse.setQuantity(1L);
        
        when(kardexRestMapper.toDomain(minRequest)).thenReturn(minKardex);
        when(kardexCommandPort.registerSale(minKardex)).thenReturn(minKardex);
        when(kardexResponseMapper.toDtoResponse(minKardex)).thenReturn(minResponse);
        
        // Act
        ResponseEntity<ResponseDto<KardexDtoResponse>> response = kardexCommandController.saleKardex(minRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getBody().getStatus());
        assertEquals(1L, response.getBody().getData().getQuantity());
        
        verify(kardexCommandPort).registerSale(minKardex);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle purchase with large quantities")
    void testPurchaseKardexWithLargeQuantity() {
        // Arrange
        KardexPurchaseDtoRequest largeRequest = new KardexPurchaseDtoRequest();
        largeRequest.setProductId(100L);
        largeRequest.setQuantity(10000L);
        largeRequest.setUnitPrice(new BigDecimal("999.99"));
        
        Kardex largeKardex = new Kardex();
        largeKardex.setId(5L);
        largeKardex.setQuantity(10000);
        largeKardex.setUnitPrice(new BigDecimal("999.99"));
        
        KardexDtoResponse largeResponse = new KardexDtoResponse();
        largeResponse.setId(5L);
        largeResponse.setQuantity(10000L);
        
        when(kardexRestMapper.toDomain(largeRequest)).thenReturn(largeKardex);
        when(kardexCommandPort.registerPurchase(largeKardex)).thenReturn(largeKardex);
        when(kardexResponseMapper.toDtoResponse(largeKardex)).thenReturn(largeResponse);
        
        // Act
        ResponseEntity<ResponseDto<KardexDtoResponse>> response = kardexCommandController.purchaseKardex(largeRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(10000L, response.getBody().getData().getQuantity());
        
        verify(kardexCommandPort).registerPurchase(largeKardex);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should verify purchase response structure")
    void testPurchaseKardexResponseStructure() {
        // Arrange
        when(kardexRestMapper.toDomain(purchaseRequest)).thenReturn(purchaseKardex);
        when(kardexCommandPort.registerPurchase(purchaseKardex)).thenReturn(purchaseKardex);
        when(kardexResponseMapper.toDtoResponse(purchaseKardex)).thenReturn(purchaseResponse);
        
        // Act
        ResponseEntity<ResponseDto<KardexDtoResponse>> response = kardexCommandController.purchaseKardex(purchaseRequest);
        
        // Assert
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertNotNull(response.getBody().getMessage());
        assertTrue(response.getBody().getStatus() > 0);
        assertTrue(response.getBody().getData() instanceof KardexDtoResponse);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should verify sale response structure")
    void testSaleKardexResponseStructure() {
        // Arrange
        when(kardexRestMapper.toDomain(saleRequest)).thenReturn(saleKardex);
        when(kardexCommandPort.registerSale(saleKardex)).thenReturn(saleKardex);
        when(kardexResponseMapper.toDtoResponse(saleKardex)).thenReturn(saleResponse);
        
        // Act
        ResponseEntity<ResponseDto<KardexDtoResponse>> response = kardexCommandController.saleKardex(saleRequest);
        
        // Assert
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertNotNull(response.getBody().getMessage());
        assertTrue(response.getBody().getStatus() > 0);
        assertTrue(response.getBody().getData() instanceof KardexDtoResponse);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle purchase with details")
    void testPurchaseKardexWithDetails() {
        // Arrange
        purchaseRequest.setDetails("Urgent purchase for project X");
        
        when(kardexRestMapper.toDomain(purchaseRequest)).thenReturn(purchaseKardex);
        when(kardexCommandPort.registerPurchase(purchaseKardex)).thenReturn(purchaseKardex);
        when(kardexResponseMapper.toDtoResponse(purchaseKardex)).thenReturn(purchaseResponse);
        
        // Act
        ResponseEntity<ResponseDto<KardexDtoResponse>> response = kardexCommandController.purchaseKardex(purchaseRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getBody().getStatus());
        
        verify(kardexRestMapper).toDomain(purchaseRequest);
        verify(kardexCommandPort).registerPurchase(purchaseKardex);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle sale with details")
    void testSaleKardexWithDetails() {
        // Arrange
        saleRequest.setDetails("Sale with 10% discount");
        
        when(kardexRestMapper.toDomain(saleRequest)).thenReturn(saleKardex);
        when(kardexCommandPort.registerSale(saleKardex)).thenReturn(saleKardex);
        when(kardexResponseMapper.toDtoResponse(saleKardex)).thenReturn(saleResponse);
        
        // Act
        ResponseEntity<ResponseDto<KardexDtoResponse>> response = kardexCommandController.saleKardex(saleRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getBody().getStatus());
        
        verify(kardexRestMapper).toDomain(saleRequest);
        verify(kardexCommandPort).registerSale(saleKardex);
    }
    
    @Test
    @DisplayName("Should verify mapper interactions for purchase")
    void testPurchaseKardexMapperInteractions() {
        // Arrange
        when(kardexRestMapper.toDomain(purchaseRequest)).thenReturn(purchaseKardex);
        when(kardexCommandPort.registerPurchase(purchaseKardex)).thenReturn(purchaseKardex);
        when(kardexResponseMapper.toDtoResponse(purchaseKardex)).thenReturn(purchaseResponse);
        
        // Act
        kardexCommandController.purchaseKardex(purchaseRequest);
        
        // Assert
        verify(kardexRestMapper, times(1)).toDomain(purchaseRequest);
        verify(kardexResponseMapper, times(1)).toDtoResponse(purchaseKardex);
        verifyNoMoreInteractions(kardexRestMapper, kardexResponseMapper);
    }
    
    @Test
    @DisplayName("Should verify mapper interactions for sale")
    void testSaleKardexMapperInteractions() {
        // Arrange
        when(kardexRestMapper.toDomain(saleRequest)).thenReturn(saleKardex);
        when(kardexCommandPort.registerSale(saleKardex)).thenReturn(saleKardex);
        when(kardexResponseMapper.toDtoResponse(saleKardex)).thenReturn(saleResponse);
        
        // Act
        kardexCommandController.saleKardex(saleRequest);
        
        // Assert
        verify(kardexRestMapper, times(1)).toDomain(saleRequest);
        verify(kardexResponseMapper, times(1)).toDtoResponse(saleKardex);
        verifyNoMoreInteractions(kardexRestMapper, kardexResponseMapper);
    }
    
    @Test
    @DisplayName("Should verify deleteAll is called only once")
    void testDeleteAllKardexVerifyOnce() {
        // Arrange
        doNothing().when(kardexCommandPort).deleteAll();
        
        // Act
        kardexCommandController.deleteAllKardex();
        
        // Assert
        verify(kardexCommandPort, times(1)).deleteAll();
        verifyNoMoreInteractions(kardexCommandPort);
    }
}
