package com.kardex.unit.infrastructure.adapters.output.remoteSync.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.kardex.infrastructure.adapters.output.remoteSync.config.ClientProperties;

/**
 * @brief Unit tests for ClientProperties
 * 
 * Tests the configuration properties for remote HTTP clients.
 */
@DisplayName("ClientProperties Tests")
class ClientPropertiesUnitTest {

    private ClientProperties clientProperties;

    @BeforeEach
    void setUp() {
        clientProperties = new ClientProperties();
    }

    @Test
    @DisplayName("Should initialize with default Stock and Products objects")
    void testDefaultInitialization() {
        // Assert
        assertNotNull(clientProperties.getStock());
        assertNotNull(clientProperties.getProducts());
    }

    @Test
    @DisplayName("Should set and get stock base URL")
    void testSetAndGetStockBaseUrl() {
        // Arrange
        String expectedUrl = "http://localhost:8081/stock";

        // Act
        clientProperties.getStock().setBaseUrl(expectedUrl);

        // Assert
        assertEquals(expectedUrl, clientProperties.getStock().getBaseUrl());
    }

    @Test
    @DisplayName("Should set and get products base URL")
    void testSetAndGetProductsBaseUrl() {
        // Arrange
        String expectedUrl = "http://localhost:8082/products";

        // Act
        clientProperties.getProducts().setBaseUrl(expectedUrl);

        // Assert
        assertEquals(expectedUrl, clientProperties.getProducts().getBaseUrl());
    }

    @Test
    @DisplayName("Should validate successfully with valid URLs")
    void testValidate_WithValidUrls() {
        // Arrange
        clientProperties.getStock().setBaseUrl("http://localhost:8081/stock");
        clientProperties.getProducts().setBaseUrl("http://localhost:8082/products");

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> clientProperties.validate());
    }

    @Test
    @DisplayName("Should throw exception when stock base URL is null")
    void testValidate_WithNullStockUrl() {
        // Arrange
        clientProperties.getStock().setBaseUrl(null);
        clientProperties.getProducts().setBaseUrl("http://localhost:8082/products");

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            clientProperties.validate();
        });

        assertTrue(exception.getMessage().contains("Stock base URL not configured"));
        assertTrue(exception.getMessage().contains("services.stock.base-url"));
    }

    @Test
    @DisplayName("Should throw exception when stock base URL is empty")
    void testValidate_WithEmptyStockUrl() {
        // Arrange
        clientProperties.getStock().setBaseUrl("");
        clientProperties.getProducts().setBaseUrl("http://localhost:8082/products");

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            clientProperties.validate();
        });

        assertTrue(exception.getMessage().contains("Stock base URL not configured"));
    }

    @Test
    @DisplayName("Should throw exception when stock base URL is blank")
    void testValidate_WithBlankStockUrl() {
        // Arrange
        clientProperties.getStock().setBaseUrl("   ");
        clientProperties.getProducts().setBaseUrl("http://localhost:8082/products");

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            clientProperties.validate();
        });

        assertTrue(exception.getMessage().contains("Stock base URL not configured"));
    }

    @Test
    @DisplayName("Should throw exception when products base URL is null")
    void testValidate_WithNullProductsUrl() {
        // Arrange
        clientProperties.getStock().setBaseUrl("http://localhost:8081/stock");
        clientProperties.getProducts().setBaseUrl(null);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            clientProperties.validate();
        });

        assertTrue(exception.getMessage().contains("Products base URL not configured"));
        assertTrue(exception.getMessage().contains("services.products.base-url"));
    }

    @Test
    @DisplayName("Should throw exception when products base URL is empty")
    void testValidate_WithEmptyProductsUrl() {
        // Arrange
        clientProperties.getStock().setBaseUrl("http://localhost:8081/stock");
        clientProperties.getProducts().setBaseUrl("");

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            clientProperties.validate();
        });

        assertTrue(exception.getMessage().contains("Products base URL not configured"));
    }

    @Test
    @DisplayName("Should throw exception when products base URL is blank")
    void testValidate_WithBlankProductsUrl() {
        // Arrange
        clientProperties.getStock().setBaseUrl("http://localhost:8081/stock");
        clientProperties.getProducts().setBaseUrl("   ");

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            clientProperties.validate();
        });

        assertTrue(exception.getMessage().contains("Products base URL not configured"));
    }

    @Test
    @DisplayName("Should throw exception when both URLs are null")
    void testValidate_WithBothUrlsNull() {
        // Arrange
        clientProperties.getStock().setBaseUrl(null);
        clientProperties.getProducts().setBaseUrl(null);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            clientProperties.validate();
        });

        // Should fail on stock first
        assertTrue(exception.getMessage().contains("Stock base URL not configured"));
    }

    @Test
    @DisplayName("Should handle HTTPS URLs")
    void testSetStockBaseUrl_WithHttps() {
        // Arrange
        String httpsUrl = "https://api.example.com/stock";

        // Act
        clientProperties.getStock().setBaseUrl(httpsUrl);

        // Assert
        assertEquals(httpsUrl, clientProperties.getStock().getBaseUrl());
    }

    @Test
    @DisplayName("Should handle URLs with ports")
    void testSetProductsBaseUrl_WithPort() {
        // Arrange
        String urlWithPort = "http://localhost:9090/api/products";

        // Act
        clientProperties.getProducts().setBaseUrl(urlWithPort);

        // Assert
        assertEquals(urlWithPort, clientProperties.getProducts().getBaseUrl());
    }

    @Test
    @DisplayName("Should handle URLs with paths")
    void testSetStockBaseUrl_WithPath() {
        // Arrange
        String urlWithPath = "http://api.example.com/v1/stock/service";

        // Act
        clientProperties.getStock().setBaseUrl(urlWithPath);

        // Assert
        assertEquals(urlWithPath, clientProperties.getStock().getBaseUrl());
    }

    @Test
    @DisplayName("Should handle URLs with trailing slashes")
    void testSetProductsBaseUrl_WithTrailingSlash() {
        // Arrange
        String urlWithSlash = "http://localhost:8082/products/";

        // Act
        clientProperties.getProducts().setBaseUrl(urlWithSlash);

        // Assert
        assertEquals(urlWithSlash, clientProperties.getProducts().getBaseUrl());
    }

    @Test
    @DisplayName("Should handle localhost URLs")
    void testSetStockBaseUrl_WithLocalhost() {
        // Arrange
        String localhostUrl = "http://localhost:8080";

        // Act
        clientProperties.getStock().setBaseUrl(localhostUrl);

        // Assert
        assertEquals(localhostUrl, clientProperties.getStock().getBaseUrl());
    }

    @Test
    @DisplayName("Should handle IP address URLs")
    void testSetProductsBaseUrl_WithIpAddress() {
        // Arrange
        String ipUrl = "http://192.168.1.100:8082/products";

        // Act
        clientProperties.getProducts().setBaseUrl(ipUrl);

        // Assert
        assertEquals(ipUrl, clientProperties.getProducts().getBaseUrl());
    }

    @Test
    @DisplayName("Should handle domain URLs")
    void testSetStockBaseUrl_WithDomain() {
        // Arrange
        String domainUrl = "https://stock-service.company.com/api";

        // Act
        clientProperties.getStock().setBaseUrl(domainUrl);

        // Assert
        assertEquals(domainUrl, clientProperties.getStock().getBaseUrl());
    }

    @Test
    @DisplayName("Should allow setting both URLs independently")
    void testSetBothUrls_Independently() {
        // Arrange
        String stockUrl = "http://stock-service:8081";
        String productsUrl = "http://products-service:8082";

        // Act
        clientProperties.getStock().setBaseUrl(stockUrl);
        clientProperties.getProducts().setBaseUrl(productsUrl);

        // Assert
        assertEquals(stockUrl, clientProperties.getStock().getBaseUrl());
        assertEquals(productsUrl, clientProperties.getProducts().getBaseUrl());
        assertNotEquals(stockUrl, clientProperties.getProducts().getBaseUrl());
    }

    @Test
    @DisplayName("Should handle very long URLs")
    void testSetStockBaseUrl_WithVeryLongUrl() {
        // Arrange
        String longUrl = "http://very-long-domain-name-for-testing-purposes.example.com/api/v1/stock/service/endpoint";

        // Act
        clientProperties.getStock().setBaseUrl(longUrl);

        // Assert
        assertEquals(longUrl, clientProperties.getStock().getBaseUrl());
    }

    @Test
    @DisplayName("Should handle URLs with query parameters")
    void testSetProductsBaseUrl_WithQueryParams() {
        // Arrange
        String urlWithParams = "http://localhost:8082/products?version=v1&env=prod";

        // Act
        clientProperties.getProducts().setBaseUrl(urlWithParams);

        // Assert
        assertEquals(urlWithParams, clientProperties.getProducts().getBaseUrl());
    }

    @Test
    @DisplayName("Should handle URLs with authentication")
    void testSetStockBaseUrl_WithAuthentication() {
        // Arrange
        String urlWithAuth = "http://user:pass@localhost:8081/stock";

        // Act
        clientProperties.getStock().setBaseUrl(urlWithAuth);

        // Assert
        assertEquals(urlWithAuth, clientProperties.getStock().getBaseUrl());
    }

    @Test
    @DisplayName("Should handle URLs with fragments")
    void testSetProductsBaseUrl_WithFragment() {
        // Arrange
        String urlWithFragment = "http://localhost:8082/products#section";

        // Act
        clientProperties.getProducts().setBaseUrl(urlWithFragment);

        // Assert
        assertEquals(urlWithFragment, clientProperties.getProducts().getBaseUrl());
    }

    @Test
    @DisplayName("Should allow updating URLs")
    void testUpdateUrls() {
        // Arrange
        String initialUrl = "http://localhost:8081/stock";
        String updatedUrl = "https://production-stock.example.com/api";
        
        clientProperties.getStock().setBaseUrl(initialUrl);
        assertEquals(initialUrl, clientProperties.getStock().getBaseUrl());

        // Act
        clientProperties.getStock().setBaseUrl(updatedUrl);

        // Assert
        assertEquals(updatedUrl, clientProperties.getStock().getBaseUrl());
        assertNotEquals(initialUrl, clientProperties.getStock().getBaseUrl());
    }

    @Test
    @DisplayName("Stock inner class should have proper getters and setters")
    void testStockInnerClass() {
        // Arrange
        ClientProperties.Stock stock = new ClientProperties.Stock();
        String testUrl = "http://test.com/stock";

        // Act
        stock.setBaseUrl(testUrl);

        // Assert
        assertEquals(testUrl, stock.getBaseUrl());
    }

    @Test
    @DisplayName("Products inner class should have proper getters and setters")
    void testProductsInnerClass() {
        // Arrange
        ClientProperties.Products products = new ClientProperties.Products();
        String testUrl = "http://test.com/products";

        // Act
        products.setBaseUrl(testUrl);

        // Assert
        assertEquals(testUrl, products.getBaseUrl());
    }

    @Test
    @DisplayName("Should create new instances of Stock and Products")
    void testCreateNewInstances() {
        // Act
        ClientProperties.Stock stock1 = new ClientProperties.Stock();
        ClientProperties.Stock stock2 = new ClientProperties.Stock();
        ClientProperties.Products products1 = new ClientProperties.Products();
        ClientProperties.Products products2 = new ClientProperties.Products();

        // Assert
        assertNotSame(stock1, stock2);
        assertNotSame(products1, products2);
    }

    @Test
    @DisplayName("Should handle null assignment and retrieval")
    void testNullAssignment() {
        // Act
        clientProperties.getStock().setBaseUrl(null);

        // Assert
        assertNull(clientProperties.getStock().getBaseUrl());
    }

    @Test
    @DisplayName("Should validate with minimum valid URLs")
    void testValidate_WithMinimumValidUrls() {
        // Arrange
        clientProperties.getStock().setBaseUrl("h");
        clientProperties.getProducts().setBaseUrl("p");

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> clientProperties.validate());
    }

    @Test
    @DisplayName("Should handle special characters in URLs")
    void testSetStockBaseUrl_WithSpecialCharacters() {
        // Arrange
        String urlWithSpecialChars = "http://localhost:8081/stock-api_v2";

        // Act
        clientProperties.getStock().setBaseUrl(urlWithSpecialChars);

        // Assert
        assertEquals(urlWithSpecialChars, clientProperties.getStock().getBaseUrl());
    }

    @Test
    @DisplayName("Should handle unicode characters in URLs")
    void testSetProductsBaseUrl_WithUnicodeCharacters() {
        // Arrange
        String urlWithUnicode = "http://localhost:8082/productos";

        // Act
        clientProperties.getProducts().setBaseUrl(urlWithUnicode);

        // Assert
        assertEquals(urlWithUnicode, clientProperties.getProducts().getBaseUrl());
    }
}
