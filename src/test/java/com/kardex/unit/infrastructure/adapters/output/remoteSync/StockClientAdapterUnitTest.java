package com.kardex.unit.infrastructure.adapters.output.remoteSync;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.kardex.domain.model.Stock;
import com.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import com.kardex.infrastructure.adapters.output.remoteSync.adapter.StockClientAdapter;
import com.kardex.infrastructure.adapters.output.remoteSync.config.IStockClient;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.StockBuyDtoRequest;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.StockDtoResponse;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.StockSellDtoRequest;
import com.kardex.infrastructure.adapters.output.remoteSync.mapper.IStockClientMapper;

/**
 * @brief Unit tests for StockClientAdapter
 * 
 * Tests the remote synchronization adapter for stock operations
 * including buy and sell transactions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StockClientAdapter Unit Tests")
class StockClientAdapterUnitTest {

    @Mock
    private IStockClient stockClient;

    @Mock
    private IStockClientMapper stockClientMapper;

    @InjectMocks
    private StockClientAdapter stockClientAdapter;

    private Stock stock;
    private StockBuyDtoRequest stockBuyDtoRequest;
    private StockSellDtoRequest stockSellDtoRequest;
    private StockDtoResponse stockDtoResponse;
    private ResponseDto<StockDtoResponse> responseDto;

    @BeforeEach
    void setUp() {
        // Initialize Stock domain object
        stock = new Stock();
        stock.setProductId(1L);
        stock.setQuantity(10);
        stock.setPrice(BigDecimal.valueOf(100.00));
        stock.setEnterpriseId("ENT-123");

        // Initialize StockBuyDtoRequest
        stockBuyDtoRequest = StockBuyDtoRequest.builder()
            .productId(1L)
            .quantity(10)
            .price(BigDecimal.valueOf(100.00))
            .build();

        // Initialize StockSellDtoRequest
        stockSellDtoRequest = StockSellDtoRequest.builder()
            .productId(1L)
            .quantity(10)
            .price(BigDecimal.valueOf(100.00))
            .build();

        // Initialize StockDtoResponse
        stockDtoResponse = new StockDtoResponse();
        stockDtoResponse.setId(1L);
        stockDtoResponse.setProductId(1L);
        stockDtoResponse.setQuantity(10);
        stockDtoResponse.setPrice(BigDecimal.valueOf(100.00));
        stockDtoResponse.setEnterpriseId("ENT-123");
        stockDtoResponse.setStatus(true);

        // Initialize ResponseDto
        responseDto = new ResponseDto<>();
        responseDto.setData(stockDtoResponse);
        responseDto.setMessage("Success");
    }

    @Test
    @DisplayName("Should buy stock successfully with 200 OK")
    void testBuyStock_Success() {
        // Arrange
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.ok(responseDto);
        
        when(stockClientMapper.toDtoRequest(any(Stock.class))).thenReturn(stockBuyDtoRequest);
        when(stockClient.buyStock(any(StockBuyDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> stockClientAdapter.buyStock(stock));
        
        verify(stockClientMapper, times(1)).toDtoRequest(stock);
        verify(stockClient, times(1)).buyStock(stockBuyDtoRequest);
    }

    @Test
    @DisplayName("Should buy stock successfully with 201 Created")
    void testBuyStock_Created() {
        // Arrange
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        
        when(stockClientMapper.toDtoRequest(any(Stock.class))).thenReturn(stockBuyDtoRequest);
        when(stockClient.buyStock(any(StockBuyDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.buyStock(stock));
        
        verify(stockClientMapper, times(1)).toDtoRequest(stock);
        verify(stockClient, times(1)).buyStock(stockBuyDtoRequest);
    }

    @Test
    @DisplayName("Should throw exception when buy stock fails with 4xx")
    void testBuyStock_ClientError() {
        // Arrange
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        
        when(stockClientMapper.toDtoRequest(any(Stock.class))).thenReturn(stockBuyDtoRequest);
        when(stockClient.buyStock(any(StockBuyDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            stockClientAdapter.buyStock(stock)
        );
        
        assertEquals("Failed to buy stock", exception.getMessage());
        verify(stockClientMapper, times(1)).toDtoRequest(stock);
        verify(stockClient, times(1)).buyStock(stockBuyDtoRequest);
    }

    @Test
    @DisplayName("Should throw exception when buy stock fails with 5xx")
    void testBuyStock_ServerError() {
        // Arrange
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
        
        when(stockClientMapper.toDtoRequest(any(Stock.class))).thenReturn(stockBuyDtoRequest);
        when(stockClient.buyStock(any(StockBuyDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            stockClientAdapter.buyStock(stock)
        );
        
        assertEquals("Failed to buy stock", exception.getMessage());
    }

    @Test
    @DisplayName("Should sell stock successfully with 200 OK")
    void testSellStock_Success() {
        // Arrange
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.ok(responseDto);
        
        when(stockClientMapper.toSellDtoRequest(any(Stock.class))).thenReturn(stockSellDtoRequest);
        when(stockClient.sellStock(any(StockSellDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.sellStock(stock));
        
        verify(stockClientMapper, times(1)).toSellDtoRequest(stock);
        verify(stockClient, times(1)).sellStock(stockSellDtoRequest);
    }

    @Test
    @DisplayName("Should sell stock successfully with 202 Accepted")
    void testSellStock_Accepted() {
        // Arrange
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.status(HttpStatus.ACCEPTED).body(responseDto);
        
        when(stockClientMapper.toSellDtoRequest(any(Stock.class))).thenReturn(stockSellDtoRequest);
        when(stockClient.sellStock(any(StockSellDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.sellStock(stock));
        
        verify(stockClientMapper, times(1)).toSellDtoRequest(stock);
        verify(stockClient, times(1)).sellStock(stockSellDtoRequest);
    }

    @Test
    @DisplayName("Should throw exception when sell stock fails with 4xx")
    void testSellStock_ClientError() {
        // Arrange
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDto);
        
        when(stockClientMapper.toSellDtoRequest(any(Stock.class))).thenReturn(stockSellDtoRequest);
        when(stockClient.sellStock(any(StockSellDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            stockClientAdapter.sellStock(stock)
        );
        
        assertEquals("Failed to sell stock", exception.getMessage());
        verify(stockClientMapper, times(1)).toSellDtoRequest(stock);
        verify(stockClient, times(1)).sellStock(stockSellDtoRequest);
    }

    @Test
    @DisplayName("Should throw exception when sell stock fails with 5xx")
    void testSellStock_ServerError() {
        // Arrange
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(responseDto);
        
        when(stockClientMapper.toSellDtoRequest(any(Stock.class))).thenReturn(stockSellDtoRequest);
        when(stockClient.sellStock(any(StockSellDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            stockClientAdapter.sellStock(stock)
        );
        
        assertEquals("Failed to sell stock", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle buy stock with large quantity")
    void testBuyStock_LargeQuantity() {
        // Arrange
        stock.setQuantity(999999);
        stockBuyDtoRequest.setQuantity(999999);
        
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.ok(responseDto);
        
        when(stockClientMapper.toDtoRequest(any(Stock.class))).thenReturn(stockBuyDtoRequest);
        when(stockClient.buyStock(any(StockBuyDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.buyStock(stock));
        
        verify(stockClient, times(1)).buyStock(stockBuyDtoRequest);
    }

    @Test
    @DisplayName("Should handle sell stock with large quantity")
    void testSellStock_LargeQuantity() {
        // Arrange
        stock.setQuantity(999999);
        stockSellDtoRequest.setQuantity(999999);
        
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.ok(responseDto);
        
        when(stockClientMapper.toSellDtoRequest(any(Stock.class))).thenReturn(stockSellDtoRequest);
        when(stockClient.sellStock(any(StockSellDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.sellStock(stock));
        
        verify(stockClient, times(1)).sellStock(stockSellDtoRequest);
    }

    @Test
    @DisplayName("Should handle buy stock with high price")
    void testBuyStock_HighPrice() {
        // Arrange
        BigDecimal highPrice = new BigDecimal("99999999.99");
        stock.setPrice(highPrice);
        stockBuyDtoRequest.setPrice(highPrice);
        
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.ok(responseDto);
        
        when(stockClientMapper.toDtoRequest(any(Stock.class))).thenReturn(stockBuyDtoRequest);
        when(stockClient.buyStock(any(StockBuyDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.buyStock(stock));
        
        verify(stockClient, times(1)).buyStock(stockBuyDtoRequest);
    }

    @Test
    @DisplayName("Should handle sell stock with high price")
    void testSellStock_HighPrice() {
        // Arrange
        BigDecimal highPrice = new BigDecimal("99999999.99");
        stock.setPrice(highPrice);
        stockSellDtoRequest.setPrice(highPrice);
        
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.ok(responseDto);
        
        when(stockClientMapper.toSellDtoRequest(any(Stock.class))).thenReturn(stockSellDtoRequest);
        when(stockClient.sellStock(any(StockSellDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.sellStock(stock));
        
        verify(stockClient, times(1)).sellStock(stockSellDtoRequest);
    }

    @Test
    @DisplayName("Should handle mapper exception on buy stock")
    void testBuyStock_MapperException() {
        // Arrange
        when(stockClientMapper.toDtoRequest(any(Stock.class)))
            .thenThrow(new RuntimeException("Mapping error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            stockClientAdapter.buyStock(stock)
        );
        
        verify(stockClientMapper, times(1)).toDtoRequest(stock);
        verify(stockClient, never()).buyStock(any());
    }

    @Test
    @DisplayName("Should handle mapper exception on sell stock")
    void testSellStock_MapperException() {
        // Arrange
        when(stockClientMapper.toSellDtoRequest(any(Stock.class)))
            .thenThrow(new RuntimeException("Mapping error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            stockClientAdapter.sellStock(stock)
        );
        
        verify(stockClientMapper, times(1)).toSellDtoRequest(stock);
        verify(stockClient, never()).sellStock(any());
    }

    @Test
    @DisplayName("Should handle client exception on buy stock")
    void testBuyStock_ClientException() {
        // Arrange
        when(stockClientMapper.toDtoRequest(any(Stock.class))).thenReturn(stockBuyDtoRequest);
        when(stockClient.buyStock(any(StockBuyDtoRequest.class)))
            .thenThrow(new RuntimeException("Network error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            stockClientAdapter.buyStock(stock)
        );
        
        verify(stockClient, times(1)).buyStock(stockBuyDtoRequest);
    }

    @Test
    @DisplayName("Should handle client exception on sell stock")
    void testSellStock_ClientException() {
        // Arrange
        when(stockClientMapper.toSellDtoRequest(any(Stock.class))).thenReturn(stockSellDtoRequest);
        when(stockClient.sellStock(any(StockSellDtoRequest.class)))
            .thenThrow(new RuntimeException("Network error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            stockClientAdapter.sellStock(stock)
        );
        
        verify(stockClient, times(1)).sellStock(stockSellDtoRequest);
    }

    @Test
    @DisplayName("Should handle buy stock with different product IDs")
    void testBuyStock_DifferentProductIds() {
        // Arrange
        stock.setProductId(999L);
        stockBuyDtoRequest.setProductId(999L);
        
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.ok(responseDto);
        
        when(stockClientMapper.toDtoRequest(any(Stock.class))).thenReturn(stockBuyDtoRequest);
        when(stockClient.buyStock(any(StockBuyDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.buyStock(stock));
        
        verify(stockClient, times(1)).buyStock(stockBuyDtoRequest);
    }

    @Test
    @DisplayName("Should handle sell stock with different product IDs")
    void testSellStock_DifferentProductIds() {
        // Arrange
        stock.setProductId(999L);
        stockSellDtoRequest.setProductId(999L);
        
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.ok(responseDto);
        
        when(stockClientMapper.toSellDtoRequest(any(Stock.class))).thenReturn(stockSellDtoRequest);
        when(stockClient.sellStock(any(StockSellDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.sellStock(stock));
        
        verify(stockClient, times(1)).sellStock(stockSellDtoRequest);
    }

    @Test
    @DisplayName("Should handle buy stock with different enterprise IDs")
    void testBuyStock_DifferentEnterpriseIds() {
        // Arrange
        stock.setEnterpriseId("ENT-456");
        
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.ok(responseDto);
        
        when(stockClientMapper.toDtoRequest(any(Stock.class))).thenReturn(stockBuyDtoRequest);
        when(stockClient.buyStock(any(StockBuyDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.buyStock(stock));
        
        verify(stockClient, times(1)).buyStock(stockBuyDtoRequest);
    }

    @Test
    @DisplayName("Should handle sell stock with different enterprise IDs")
    void testSellStock_DifferentEnterpriseIds() {
        // Arrange
        stock.setEnterpriseId("ENT-789");
        
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.ok(responseDto);
        
        when(stockClientMapper.toSellDtoRequest(any(Stock.class))).thenReturn(stockSellDtoRequest);
        when(stockClient.sellStock(any(StockSellDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.sellStock(stock));
        
        verify(stockClient, times(1)).sellStock(stockSellDtoRequest);
    }

    @Test
    @DisplayName("Should use correct mapper method for buy operation")
    void testBuyStock_UsesCorrectMapperMethod() {
        // Arrange
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.ok(responseDto);
        
        when(stockClientMapper.toDtoRequest(any(Stock.class))).thenReturn(stockBuyDtoRequest);
        when(stockClient.buyStock(any(StockBuyDtoRequest.class))).thenReturn(responseEntity);

        // Act
        stockClientAdapter.buyStock(stock);

        // Assert
        verify(stockClientMapper, times(1)).toDtoRequest(stock);
        verify(stockClientMapper, never()).toSellDtoRequest(any());
    }

    @Test
    @DisplayName("Should use correct mapper method for sell operation")
    void testSellStock_UsesCorrectMapperMethod() {
        // Arrange
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.ok(responseDto);
        
        when(stockClientMapper.toSellDtoRequest(any(Stock.class))).thenReturn(stockSellDtoRequest);
        when(stockClient.sellStock(any(StockSellDtoRequest.class))).thenReturn(responseEntity);

        // Act
        stockClientAdapter.sellStock(stock);

        // Assert
        verify(stockClientMapper, times(1)).toSellDtoRequest(stock);
        verify(stockClientMapper, never()).toDtoRequest(any());
    }

    @Test
    @DisplayName("Should handle buy stock with minimum valid quantity")
    void testBuyStock_MinimumQuantity() {
        // Arrange
        stock.setQuantity(1);
        stockBuyDtoRequest.setQuantity(1);
        
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.ok(responseDto);
        
        when(stockClientMapper.toDtoRequest(any(Stock.class))).thenReturn(stockBuyDtoRequest);
        when(stockClient.buyStock(any(StockBuyDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.buyStock(stock));
    }

    @Test
    @DisplayName("Should handle sell stock with minimum valid quantity")
    void testSellStock_MinimumQuantity() {
        // Arrange
        stock.setQuantity(1);
        stockSellDtoRequest.setQuantity(1);
        
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.ok(responseDto);
        
        when(stockClientMapper.toSellDtoRequest(any(Stock.class))).thenReturn(stockSellDtoRequest);
        when(stockClient.sellStock(any(StockSellDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.sellStock(stock));
    }

    @Test
    @DisplayName("Should handle buy stock with precise decimal price")
    void testBuyStock_PreciseDecimalPrice() {
        // Arrange
        BigDecimal precisePrice = new BigDecimal("123.456789");
        stock.setPrice(precisePrice);
        stockBuyDtoRequest.setPrice(precisePrice);
        
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.ok(responseDto);
        
        when(stockClientMapper.toDtoRequest(any(Stock.class))).thenReturn(stockBuyDtoRequest);
        when(stockClient.buyStock(any(StockBuyDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.buyStock(stock));
    }

    @Test
    @DisplayName("Should handle sell stock with precise decimal price")
    void testSellStock_PreciseDecimalPrice() {
        // Arrange
        BigDecimal precisePrice = new BigDecimal("987.654321");
        stock.setPrice(precisePrice);
        stockSellDtoRequest.setPrice(precisePrice);
        
        ResponseEntity<ResponseDto<StockDtoResponse>> responseEntity = 
            ResponseEntity.ok(responseDto);
        
        when(stockClientMapper.toSellDtoRequest(any(Stock.class))).thenReturn(stockSellDtoRequest);
        when(stockClient.sellStock(any(StockSellDtoRequest.class))).thenReturn(responseEntity);

        // Act & Assert
        assertDoesNotThrow(() -> stockClientAdapter.sellStock(stock));
    }
}
