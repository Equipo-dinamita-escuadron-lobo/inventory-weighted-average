package com.kardex.unit.infrastructure.adapters.output.multitenancy.interceptor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.WebRequest;

import com.kardex.infrastructure.adapters.output.messageBroker.aspect.JwtTokenService;
import com.kardex.infrastructure.adapters.output.multitenancy.interceptor.TenantInterceptor;
import com.kardex.infrastructure.adapters.output.multitenancy.utils.TenantContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantInterceptor - Tests de interceptor de tenant")
class TenantInterceptorUnitTest {

    @InjectMocks
    private TenantInterceptor tenantInterceptor;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private WebRequest webRequest;

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    // ==================== Tests para preHandle ====================

    @Test
    @DisplayName("preHandle - Establece tenantId en contexto desde JWT")
    void preHandle_SetsTenantIdInContextFromJwt() throws Exception {
        // Arrange
        String expectedTenantId = "tenant-123";
        when(jwtTokenService.getTenantId()).thenReturn(expectedTenantId);

        // Act
        tenantInterceptor.preHandle(webRequest);

        // Assert
        assertEquals(expectedTenantId, TenantContext.getTenantId());
        verify(jwtTokenService, times(1)).getTenantId();
    }

    @Test
    @DisplayName("preHandle - Lanza excepción cuando jwtTokenService falla")
    void preHandle_ThrowsExceptionWhenJwtTokenServiceFails() {
        // Arrange
        when(jwtTokenService.getTenantId()).thenThrow(new RuntimeException("JWT error"));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, 
            () -> tenantInterceptor.preHandle(webRequest)
        );

        assertTrue(exception.getMessage().contains("Could not establish tenant context from JWT"));
        assertNull(TenantContext.getTenantId());
    }

    @Test
    @DisplayName("preHandle - Con tenantId null establece null en contexto")
    void preHandle_WithNullTenantId_SetsNullInContext() throws Exception {
        // Arrange
        when(jwtTokenService.getTenantId()).thenReturn(null);

        // Act
        tenantInterceptor.preHandle(webRequest);

        // Assert
        assertNull(TenantContext.getTenantId());
    }

    @Test
    @DisplayName("preHandle - Con diferentes tenantIds establece correctamente cada uno")
    void preHandle_WithDifferentTenantIds_SetsEachCorrectly() throws Exception {
        // Arrange
        String tenant1 = "tenant-001";
        String tenant2 = "tenant-002";

        when(jwtTokenService.getTenantId()).thenReturn(tenant1);
        tenantInterceptor.preHandle(webRequest);
        String firstTenant = TenantContext.getTenantId();

        TenantContext.clear();

        when(jwtTokenService.getTenantId()).thenReturn(tenant2);
        tenantInterceptor.preHandle(webRequest);
        String secondTenant = TenantContext.getTenantId();

        // Assert
        assertEquals(tenant1, firstTenant);
        assertEquals(tenant2, secondTenant);
    }

    // ==================== Tests para postHandle ====================

    @Test
    @DisplayName("postHandle - Limpia contexto de tenant")
    void postHandle_ClearsTenantContext() throws Exception {
        // Arrange
        TenantContext.setTenantId("tenant-456");

        // Act
        tenantInterceptor.postHandle(webRequest, null);

        // Assert
        assertNull(TenantContext.getTenantId());
    }

    @Test
    @DisplayName("postHandle - No lanza excepción cuando contexto está vacío")
    void postHandle_DoesNotThrowExceptionWhenContextIsEmpty() {
        // Act & Assert
        assertDoesNotThrow(() -> tenantInterceptor.postHandle(webRequest, null));
    }

    @Test
    @DisplayName("postHandle - Puede ser llamado múltiples veces sin error")
    void postHandle_CanBeCalledMultipleTimesWithoutError() {
        // Arrange
        TenantContext.setTenantId("tenant-789");

        // Act & Assert
        assertDoesNotThrow(() -> {
            tenantInterceptor.postHandle(webRequest, null);
            tenantInterceptor.postHandle(webRequest, null);
            tenantInterceptor.postHandle(webRequest, null);
        });
    }

    // ==================== Tests para afterCompletion ====================

    @Test
    @DisplayName("afterCompletion - Completa sin errores")
    void afterCompletion_CompletesWithoutErrors() {
        // Act & Assert
        assertDoesNotThrow(() -> tenantInterceptor.afterCompletion(webRequest, null));
    }

    @Test
    @DisplayName("afterCompletion - Con excepción completa sin errores")
    void afterCompletion_WithException_CompletesWithoutErrors() {
        // Arrange
        Exception requestException = new RuntimeException("Request failed");

        // Act & Assert
        assertDoesNotThrow(() -> tenantInterceptor.afterCompletion(webRequest, requestException));
    }

    @Test
    @DisplayName("afterCompletion - No modifica contexto de tenant")
    void afterCompletion_DoesNotModifyTenantContext() throws Exception {
        // Arrange
        String expectedTenantId = "tenant-xyz";
        TenantContext.setTenantId(expectedTenantId);

        // Act
        tenantInterceptor.afterCompletion(webRequest, null);

        // Assert
        assertEquals(expectedTenantId, TenantContext.getTenantId());
    }

    // ==================== Tests de ciclo completo ====================

    @Test
    @DisplayName("Ciclo completo - preHandle, postHandle, afterCompletion funcionan correctamente")
    void fullCycle_PreHandlePostHandleAfterCompletion_WorkCorrectly() throws Exception {
        // Arrange
        String tenantId = "tenant-full-cycle";
        when(jwtTokenService.getTenantId()).thenReturn(tenantId);

        // Act & Assert
        // preHandle establece el tenant
        tenantInterceptor.preHandle(webRequest);
        assertEquals(tenantId, TenantContext.getTenantId());

        // postHandle limpia el tenant
        tenantInterceptor.postHandle(webRequest, null);
        assertNull(TenantContext.getTenantId());

        // afterCompletion no hace nada
        assertDoesNotThrow(() -> tenantInterceptor.afterCompletion(webRequest, null));
    }

    @Test
    @DisplayName("Ciclo con error - postHandle limpia contexto incluso si preHandle falló")
    void cycleWithError_PostHandleClearsContextEvenIfPreHandleFailed() throws Exception {
        // Arrange
        TenantContext.setTenantId("tenant-before-error");
        when(jwtTokenService.getTenantId()).thenThrow(new RuntimeException("JWT error"));

        // Act
        assertThrows(Exception.class, () -> tenantInterceptor.preHandle(webRequest));
        tenantInterceptor.postHandle(webRequest, null);

        // Assert
        assertNull(TenantContext.getTenantId());
    }

    // ==================== Tests de casos edge ====================

    @Test
    @DisplayName("preHandle - Con string vacío como tenantId establece correctamente")
    void preHandle_WithEmptyStringAsTenantId_SetsCorrectly() throws Exception {
        // Arrange
        when(jwtTokenService.getTenantId()).thenReturn("");

        // Act
        tenantInterceptor.preHandle(webRequest);

        // Assert
        assertEquals("", TenantContext.getTenantId());
    }

    @Test
    @DisplayName("preHandle - Excepción contiene causa original")
    void preHandle_ExceptionContainsOriginalCause() {
        // Arrange
        RuntimeException originalException = new RuntimeException("Original JWT error");
        when(jwtTokenService.getTenantId()).thenThrow(originalException);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, 
            () -> tenantInterceptor.preHandle(webRequest)
        );

        assertEquals(originalException, exception.getCause());
    }
}
