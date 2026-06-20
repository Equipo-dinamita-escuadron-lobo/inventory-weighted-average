package com.kardex.unit.infrastructure.adapters.output.remoteSync;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kardex.domain.model.Product;
import com.kardex.infrastructure.adapters.output.remoteSync.adapter.ProductClientAdapter;
import com.kardex.infrastructure.adapters.output.remoteSync.config.IProductClient;
import com.kardex.infrastructure.adapters.output.remoteSync.dto.ProductSyncDto;
import com.kardex.infrastructure.adapters.output.remoteSync.mapper.IProductClientMapper;

/**
 * @brief Unit tests for ProductClientAdapter
 * 
 * Tests the remote synchronization adapter for retrieving products
 * from external services.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductClientAdapter Unit Tests")
class ProductClientAdapterUnitTest {

    @Mock
    private IProductClientMapper productClientMapper;

    @Mock
    private IProductClient productClient;

    @InjectMocks
    private ProductClientAdapter productClientAdapter;

    private String enterpriseId;
    private Instant since;
    private List<ProductSyncDto> productSyncDtos;
    private List<Product> products;

    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-123";
        since = Instant.now().minusSeconds(3600); // 1 hour ago

        // Initialize ProductSyncDto list
        productSyncDtos = new ArrayList<>();
        
        ProductSyncDto dto1 = new ProductSyncDto();
        dto1.setProductId(1L);
        dto1.setName("Product 1");
        dto1.setReference("REF-001");
        dto1.setEnterpriseId(enterpriseId);
        dto1.setPresentation("Box");
        dto1.setState(true);
        productSyncDtos.add(dto1);

        ProductSyncDto dto2 = new ProductSyncDto();
        dto2.setProductId(2L);
        dto2.setName("Product 2");
        dto2.setReference("REF-002");
        dto2.setEnterpriseId(enterpriseId);
        dto2.setPresentation("Pack");
        dto2.setState(true);
        productSyncDtos.add(dto2);

        // Initialize Product domain list
        products = new ArrayList<>();
        
        Product product1 = new Product();
        product1.setProductId(1L);
        product1.setName("Product 1");
        product1.setReference("REF-001");
        product1.setEnterpriseId(enterpriseId);
        product1.setPresentation("Box");
        products.add(product1);

        Product product2 = new Product();
        product2.setProductId(2L);
        product2.setName("Product 2");
        product2.setReference("REF-002");
        product2.setEnterpriseId(enterpriseId);
        product2.setPresentation("Pack");
        products.add(product2);
    }

    @Test
    @DisplayName("Should retrieve all products by enterprise ID successfully")
    void testFindAllProductsByEnterpriseId_Success() {
        // Arrange
        when(productClient.findAllProductsByEnterpriseId(enterpriseId, since))
            .thenReturn(productSyncDtos);
        when(productClientMapper.toDomain(any(ProductSyncDto.class)))
            .thenReturn(products.get(0), products.get(1));

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Product 1", result.get(0).getName());
        assertEquals("Product 2", result.get(1).getName());
        
        verify(productClient, times(1)).findAllProductsByEnterpriseId(enterpriseId, since);
        verify(productClientMapper, times(2)).toDomain(any(ProductSyncDto.class));
    }

    @Test
    @DisplayName("Should return empty list when no products found")
    void testFindAllProductsByEnterpriseId_EmptyList() {
        // Arrange
        when(productClient.findAllProductsByEnterpriseId(enterpriseId, since))
            .thenReturn(new ArrayList<>());

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(productClient, times(1)).findAllProductsByEnterpriseId(enterpriseId, since);
        verify(productClientMapper, never()).toDomain(any(ProductSyncDto.class));
    }

    @Test
    @DisplayName("Should retrieve single product successfully")
    void testFindAllProductsByEnterpriseId_SingleProduct() {
        // Arrange
        List<ProductSyncDto> singleDto = List.of(productSyncDtos.get(0));
        when(productClient.findAllProductsByEnterpriseId(enterpriseId, since))
            .thenReturn(singleDto);
        when(productClientMapper.toDomain(any(ProductSyncDto.class)))
            .thenReturn(products.get(0));

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Product 1", result.get(0).getName());
        assertEquals("REF-001", result.get(0).getReference());
        
        verify(productClient, times(1)).findAllProductsByEnterpriseId(enterpriseId, since);
        verify(productClientMapper, times(1)).toDomain(any(ProductSyncDto.class));
    }

    @Test
    @DisplayName("Should handle multiple products successfully")
    void testFindAllProductsByEnterpriseId_MultipleProducts() {
        // Arrange
        List<ProductSyncDto> manyDtos = new ArrayList<>();
        List<Product> manyProducts = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            ProductSyncDto dto = new ProductSyncDto();
            dto.setProductId((long) i);
            dto.setName("Product " + i);
            dto.setReference("REF-" + String.format("%03d", i));
            dto.setEnterpriseId(enterpriseId);
            manyDtos.add(dto);
            
            Product product = new Product();
            product.setProductId((long) i);
            product.setName("Product " + i);
            product.setReference("REF-" + String.format("%03d", i));
            product.setEnterpriseId(enterpriseId);
            manyProducts.add(product);
        }
        
        when(productClient.findAllProductsByEnterpriseId(enterpriseId, since))
            .thenReturn(manyDtos);
        when(productClientMapper.toDomain(any(ProductSyncDto.class)))
            .thenAnswer(invocation -> {
                ProductSyncDto dto = invocation.getArgument(0);
                return manyProducts.stream()
                    .filter(p -> p.getProductId().equals(dto.getProductId()))
                    .findFirst()
                    .orElse(null);
            });

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.size());
        
        verify(productClient, times(1)).findAllProductsByEnterpriseId(enterpriseId, since);
        verify(productClientMapper, times(10)).toDomain(any(ProductSyncDto.class));
    }

    @Test
    @DisplayName("Should handle different enterprise IDs")
    void testFindAllProductsByEnterpriseId_DifferentEnterpriseIds() {
        // Arrange
        String differentEnterpriseId = "ENT-456";
        when(productClient.findAllProductsByEnterpriseId(differentEnterpriseId, since))
            .thenReturn(productSyncDtos);
        when(productClientMapper.toDomain(any(ProductSyncDto.class)))
            .thenReturn(products.get(0), products.get(1));

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(differentEnterpriseId, since);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        verify(productClient, times(1)).findAllProductsByEnterpriseId(differentEnterpriseId, since);
    }

    @Test
    @DisplayName("Should handle different timestamps")
    void testFindAllProductsByEnterpriseId_DifferentTimestamps() {
        // Arrange
        Instant differentSince = Instant.now().minusSeconds(7200); // 2 hours ago
        when(productClient.findAllProductsByEnterpriseId(enterpriseId, differentSince))
            .thenReturn(productSyncDtos);
        when(productClientMapper.toDomain(any(ProductSyncDto.class)))
            .thenReturn(products.get(0), products.get(1));

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, differentSince);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        verify(productClient, times(1)).findAllProductsByEnterpriseId(enterpriseId, differentSince);
    }

    @Test
    @DisplayName("Should handle client exception")
    void testFindAllProductsByEnterpriseId_ClientException() {
        // Arrange
        when(productClient.findAllProductsByEnterpriseId(enterpriseId, since))
            .thenThrow(new RuntimeException("Service unavailable"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since)
        );
        
        verify(productClient, times(1)).findAllProductsByEnterpriseId(enterpriseId, since);
        verify(productClientMapper, never()).toDomain(any(ProductSyncDto.class));
    }

    @Test
    @DisplayName("Should handle mapper exception")
    void testFindAllProductsByEnterpriseId_MapperException() {
        // Arrange
        when(productClient.findAllProductsByEnterpriseId(enterpriseId, since))
            .thenReturn(productSyncDtos);
        when(productClientMapper.toDomain(any(ProductSyncDto.class)))
            .thenThrow(new RuntimeException("Mapping error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since)
        );
        
        verify(productClient, times(1)).findAllProductsByEnterpriseId(enterpriseId, since);
        verify(productClientMapper, times(1)).toDomain(any(ProductSyncDto.class));
    }

    @Test
    @DisplayName("Should map all product fields correctly")
    void testFindAllProductsByEnterpriseId_FieldMapping() {
        // Arrange
        when(productClient.findAllProductsByEnterpriseId(enterpriseId, since))
            .thenReturn(List.of(productSyncDtos.get(0)));
        
        Product expectedProduct = new Product();
        expectedProduct.setProductId(1L);
        expectedProduct.setName("Product 1");
        expectedProduct.setReference("REF-001");
        expectedProduct.setEnterpriseId(enterpriseId);
        expectedProduct.setPresentation("Box");
        
        when(productClientMapper.toDomain(any(ProductSyncDto.class)))
            .thenReturn(expectedProduct);

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        Product actualProduct = result.get(0);
        assertEquals(expectedProduct.getProductId(), actualProduct.getProductId());
        assertEquals(expectedProduct.getName(), actualProduct.getName());
        assertEquals(expectedProduct.getReference(), actualProduct.getReference());
        assertEquals(expectedProduct.getEnterpriseId(), actualProduct.getEnterpriseId());
        assertEquals(expectedProduct.getPresentation(), actualProduct.getPresentation());
    }

    @Test
    @DisplayName("Should handle products with null presentation")
    void testFindAllProductsByEnterpriseId_NullPresentation() {
        // Arrange
        ProductSyncDto dtoWithNullPresentation = new ProductSyncDto();
        dtoWithNullPresentation.setProductId(3L);
        dtoWithNullPresentation.setName("Product 3");
        dtoWithNullPresentation.setReference("REF-003");
        dtoWithNullPresentation.setEnterpriseId(enterpriseId);
        dtoWithNullPresentation.setPresentation(null);
        
        when(productClient.findAllProductsByEnterpriseId(enterpriseId, since))
            .thenReturn(List.of(dtoWithNullPresentation));
        
        Product productWithNullPresentation = new Product();
        productWithNullPresentation.setProductId(3L);
        productWithNullPresentation.setName("Product 3");
        productWithNullPresentation.setReference("REF-003");
        productWithNullPresentation.setEnterpriseId(enterpriseId);
        productWithNullPresentation.setPresentation(null);
        
        when(productClientMapper.toDomain(any(ProductSyncDto.class)))
            .thenReturn(productWithNullPresentation);

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getPresentation());
    }

    @Test
    @DisplayName("Should handle instant at epoch")
    void testFindAllProductsByEnterpriseId_EpochInstant() {
        // Arrange
        Instant epoch = Instant.EPOCH;
        when(productClient.findAllProductsByEnterpriseId(enterpriseId, epoch))
            .thenReturn(productSyncDtos);
        when(productClientMapper.toDomain(any(ProductSyncDto.class)))
            .thenReturn(products.get(0), products.get(1));

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, epoch);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        verify(productClient, times(1)).findAllProductsByEnterpriseId(enterpriseId, epoch);
    }

    @Test
    @DisplayName("Should handle instant in the future")
    void testFindAllProductsByEnterpriseId_FutureInstant() {
        // Arrange
        Instant future = Instant.now().plusSeconds(3600);
        when(productClient.findAllProductsByEnterpriseId(enterpriseId, future))
            .thenReturn(new ArrayList<>());

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, future);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(productClient, times(1)).findAllProductsByEnterpriseId(enterpriseId, future);
    }

    @Test
    @DisplayName("Should preserve order of products from client")
    void testFindAllProductsByEnterpriseId_OrderPreservation() {
        // Arrange
        when(productClient.findAllProductsByEnterpriseId(enterpriseId, since))
            .thenReturn(productSyncDtos);
        when(productClientMapper.toDomain(any(ProductSyncDto.class)))
            .thenReturn(products.get(0), products.get(1));

        // Act
        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(products.get(0).getName(), result.get(0).getName());
        assertEquals(products.get(1).getName(), result.get(1).getName());
    }

    @Test
    @DisplayName("Should call mapper for each DTO")
    void testFindAllProductsByEnterpriseId_MapperInvocations() {
        // Arrange
        when(productClient.findAllProductsByEnterpriseId(enterpriseId, since))
            .thenReturn(productSyncDtos);
        when(productClientMapper.toDomain(any(ProductSyncDto.class)))
            .thenReturn(products.get(0), products.get(1));

        // Act
        productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        // Assert
        verify(productClientMapper, times(productSyncDtos.size())).toDomain(any(ProductSyncDto.class));
    }
}
