package com.kardex.unit.infrastructure.adapters.output.messageBroker.aspect;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kardex.infrastructure.adapters.output.messageBroker.aspect.JwtTokenService;
import com.kardex.infrastructure.adapters.output.security.IJwtUtils;

/**
 * @brief Unit tests for JwtTokenService
 * 
 * Tests the unified JWT token management service for both HTTP and RabbitMQ contexts.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenService Tests")
class JwtTokenServiceUnitTest {

    @Mock
    private IJwtUtils jwtUtils;

    @InjectMocks
    private JwtTokenService jwtTokenService;

    @AfterEach
    void tearDown() {
        // Clean up ThreadLocal to prevent test interference
        jwtTokenService.clearRabbitContext();
    }

    @Test
    @DisplayName("Should set and get RabbitMQ JWT token")
    void testSetAndGetRabbitJwtToken() {
        // Arrange
        String expectedToken = "rabbit.jwt.token.12345";

        // Act
        jwtTokenService.setRabbitJwtToken(expectedToken);
        String actualToken = jwtTokenService.getToken();

        // Assert
        assertEquals(expectedToken, actualToken);
    }

    @Test
    @DisplayName("Should set and get RabbitMQ tenant ID")
    void testSetAndGetRabbitTenantId() {
        // Arrange
        String expectedTenantId = "TENANT-RABBIT-001";

        // Act
        jwtTokenService.setRabbitTenantId(expectedTenantId);
        String actualTenantId = jwtTokenService.getTenantId();

        // Assert
        assertEquals(expectedTenantId, actualTenantId);
    }

    @Test
    @DisplayName("Should get token from HTTP context when RabbitMQ context is not set")
    void testGetToken_FromHttpContext() {
        // Arrange
        String httpToken = "http.jwt.token.67890";
        when(jwtUtils.getToken()).thenReturn(httpToken);

        // Act
        String actualToken = jwtTokenService.getToken();

        // Assert
        assertEquals(httpToken, actualToken);
        verify(jwtUtils, times(1)).getToken();
    }

    @Test
    @DisplayName("Should get tenant ID from HTTP context when RabbitMQ context is not set")
    void testGetTenantId_FromHttpContext() {
        // Arrange
        String httpTenantId = "TENANT-HTTP-002";
        when(jwtUtils.getId()).thenReturn(httpTenantId);

        // Act
        String actualTenantId = jwtTokenService.getTenantId();

        // Assert
        assertEquals(httpTenantId, actualTenantId);
        verify(jwtUtils, times(1)).getId();
    }

    @Test
    @DisplayName("Should prioritize RabbitMQ token over HTTP token")
    void testGetToken_PrioritizesRabbitMqContext() {
        // Arrange
        String rabbitToken = "rabbit.token";
        jwtTokenService.setRabbitJwtToken(rabbitToken);

        // Act
        String actualToken = jwtTokenService.getToken();

        // Assert
        assertEquals(rabbitToken, actualToken);
        verify(jwtUtils, never()).getToken();
    }

    @Test
    @DisplayName("Should prioritize RabbitMQ tenant ID over HTTP tenant ID")
    void testGetTenantId_PrioritizesRabbitMqContext() {
        // Arrange
        String rabbitTenantId = "RABBIT-TENANT";
        jwtTokenService.setRabbitTenantId(rabbitTenantId);

        // Act
        String actualTenantId = jwtTokenService.getTenantId();

        // Assert
        assertEquals(rabbitTenantId, actualTenantId);
        verify(jwtUtils, never()).getId();
    }

    @Test
    @DisplayName("Should throw exception when no token available in any context")
    void testGetToken_ThrowsExceptionWhenNoTokenAvailable() {
        // Arrange
        when(jwtUtils.getToken()).thenThrow(new RuntimeException("No token in HTTP context"));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            jwtTokenService.getToken();
        });

        assertTrue(exception.getMessage().contains("No hay token JWT disponible"));
        verify(jwtUtils, times(1)).getToken();
    }

    @Test
    @DisplayName("Should throw exception when no tenant ID available in any context")
    void testGetTenantId_ThrowsExceptionWhenNoTenantIdAvailable() {
        // Arrange
        when(jwtUtils.getId()).thenThrow(new RuntimeException("No tenant ID in HTTP context"));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            jwtTokenService.getTenantId();
        });

        assertTrue(exception.getMessage().contains("No hay tenant ID disponible"));
        verify(jwtUtils, times(1)).getId();
    }

    @Test
    @DisplayName("Should clear RabbitMQ context")
    void testClearRabbitContext() {
        // Arrange
        jwtTokenService.setRabbitJwtToken("token");
        jwtTokenService.setRabbitTenantId("tenant");
        when(jwtUtils.getToken()).thenReturn("http-token");
        when(jwtUtils.getId()).thenReturn("http-tenant");

        // Act
        jwtTokenService.clearRabbitContext();

        // Assert
        assertEquals("http-token", jwtTokenService.getToken());
        assertEquals("http-tenant", jwtTokenService.getTenantId());
        verify(jwtUtils, times(1)).getToken();
        verify(jwtUtils, times(1)).getId();
    }

    @Test
    @DisplayName("Should return true when in RabbitMQ context")
    void testIsInRabbitContext_ReturnsTrue() {
        // Arrange
        jwtTokenService.setRabbitJwtToken("rabbit-token");

        // Act
        boolean isInRabbitContext = jwtTokenService.isInRabbitContext();

        // Assert
        assertTrue(isInRabbitContext);
    }

    @Test
    @DisplayName("Should return false when not in RabbitMQ context")
    void testIsInRabbitContext_ReturnsFalse() {
        // Act
        boolean isInRabbitContext = jwtTokenService.isInRabbitContext();

        // Assert
        assertFalse(isInRabbitContext);
    }

    @Test
    @DisplayName("Should handle multiple set and get operations")
    void testMultipleSetAndGetOperations() {
        // Arrange & Act
        jwtTokenService.setRabbitJwtToken("token1");
        jwtTokenService.setRabbitTenantId("tenant1");
        
        String token1 = jwtTokenService.getToken();
        String tenant1 = jwtTokenService.getTenantId();
        
        jwtTokenService.setRabbitJwtToken("token2");
        jwtTokenService.setRabbitTenantId("tenant2");
        
        String token2 = jwtTokenService.getToken();
        String tenant2 = jwtTokenService.getTenantId();

        // Assert
        assertEquals("token1", token1);
        assertEquals("tenant1", tenant1);
        assertEquals("token2", token2);
        assertEquals("tenant2", tenant2);
    }

    @Test
    @DisplayName("Should handle null values from HTTP context gracefully")
    void testGetToken_WithNullFromHttpContext() {
        // Arrange
        when(jwtUtils.getToken()).thenReturn(null);

        // Act
        String token = jwtTokenService.getToken();

        // Assert
        assertNull(token);
        verify(jwtUtils, times(1)).getToken();
    }

    @Test
    @DisplayName("Should handle empty token from RabbitMQ context")
    void testGetToken_WithEmptyRabbitToken() {
        // Arrange
        jwtTokenService.setRabbitJwtToken("");

        // Act
        String token = jwtTokenService.getToken();

        // Assert
        assertEquals("", token);
        verify(jwtUtils, never()).getToken();
    }

    @Test
    @DisplayName("Should handle empty tenant ID from RabbitMQ context")
    void testGetTenantId_WithEmptyRabbitTenantId() {
        // Arrange
        jwtTokenService.setRabbitTenantId("");

        // Act
        String tenantId = jwtTokenService.getTenantId();

        // Assert
        assertEquals("", tenantId);
        verify(jwtUtils, never()).getId();
    }

    @Test
    @DisplayName("Should maintain context isolation between threads")
    void testThreadLocalIsolation() throws InterruptedException {
        // Arrange
        String mainThreadToken = "main-thread-token";
        String mainThreadTenant = "main-thread-tenant";
        
        jwtTokenService.setRabbitJwtToken(mainThreadToken);
        jwtTokenService.setRabbitTenantId(mainThreadTenant);

        // Act - Create a new thread
        Thread otherThread = new Thread(() -> {
            // In the other thread, there should be no RabbitMQ context
            assertFalse(jwtTokenService.isInRabbitContext());
        });
        
        otherThread.start();
        otherThread.join();

        // Assert - Main thread should still have its context
        assertTrue(jwtTokenService.isInRabbitContext());
        assertEquals(mainThreadToken, jwtTokenService.getToken());
        assertEquals(mainThreadTenant, jwtTokenService.getTenantId());
    }

    @Test
    @DisplayName("Should clear only RabbitMQ context, not HTTP context")
    void testClearRabbitContext_PreservesHttpContext() {
        // Arrange
        String httpToken = "http-token";
        String httpTenantId = "http-tenant";
        
        jwtTokenService.setRabbitJwtToken("rabbit-token");
        jwtTokenService.setRabbitTenantId("rabbit-tenant");
        
        when(jwtUtils.getToken()).thenReturn(httpToken);
        when(jwtUtils.getId()).thenReturn(httpTenantId);

        // Act
        jwtTokenService.clearRabbitContext();

        // Assert
        assertFalse(jwtTokenService.isInRabbitContext());
        assertEquals(httpToken, jwtTokenService.getToken());
        assertEquals(httpTenantId, jwtTokenService.getTenantId());
    }

    @Test
    @DisplayName("Should handle consecutive clear operations")
    void testConsecutiveClearOperations() {
        // Arrange
        jwtTokenService.setRabbitJwtToken("token");
        jwtTokenService.setRabbitTenantId("tenant");

        // Act
        jwtTokenService.clearRabbitContext();
        jwtTokenService.clearRabbitContext(); // Second clear should not cause issues

        // Assert
        assertFalse(jwtTokenService.isInRabbitContext());
    }

    @Test
    @DisplayName("Should set null values in RabbitMQ context")
    void testSetNullValuesInRabbitContext() {
        // Act
        jwtTokenService.setRabbitJwtToken(null);
        jwtTokenService.setRabbitTenantId(null);

        // Assert
        assertFalse(jwtTokenService.isInRabbitContext());
    }

    @Test
    @DisplayName("Should handle long token strings")
    void testHandleLongTokenStrings() {
        // Arrange
        String longToken = "Bearer " + "a".repeat(1000);
        String longTenantId = "TENANT-" + "1".repeat(500);

        // Act
        jwtTokenService.setRabbitJwtToken(longToken);
        jwtTokenService.setRabbitTenantId(longTenantId);

        // Assert
        assertEquals(longToken, jwtTokenService.getToken());
        assertEquals(longTenantId, jwtTokenService.getTenantId());
    }

    @Test
    @DisplayName("Should handle special characters in token and tenant ID")
    void testHandleSpecialCharacters() {
        // Arrange
        String tokenWithSpecialChars = "token!@#$%^&*()_+-={}[]|:;<>?,./";
        String tenantWithSpecialChars = "tenant-ñáéíóú-中文-🚀";

        // Act
        jwtTokenService.setRabbitJwtToken(tokenWithSpecialChars);
        jwtTokenService.setRabbitTenantId(tenantWithSpecialChars);

        // Assert
        assertEquals(tokenWithSpecialChars, jwtTokenService.getToken());
        assertEquals(tenantWithSpecialChars, jwtTokenService.getTenantId());
    }
}
