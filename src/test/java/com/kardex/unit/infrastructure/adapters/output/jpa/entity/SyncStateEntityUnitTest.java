package com.kardex.unit.infrastructure.adapters.output.jpa.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.kardex.infrastructure.adapters.output.jpa.entity.SyncStateEntity;

/**
 * @brief Unit tests for SyncStateEntity
 * 
 * Tests the entity behavior, lifecycle callbacks, and business logic.
 */
@DisplayName("SyncStateEntity Tests")
class SyncStateEntityUnitTest {

    private SyncStateEntity syncStateEntity;

    @BeforeEach
    void setUp() {
        syncStateEntity = new SyncStateEntity();
    }

    @Test
    @DisplayName("Should create entity with null values by default")
    void testEntityCreation_DefaultValues() {
        // Assert
        assertNull(syncStateEntity.getId());
        assertNull(syncStateEntity.getSyncType());
        assertNull(syncStateEntity.getEnterpriseId());
        assertNull(syncStateEntity.getLastSyncDate());
        assertNull(syncStateEntity.getCreatedAt());
        assertNull(syncStateEntity.getUpdatedAt());
        assertNull(syncStateEntity.getTenantId());
    }

    @Test
    @DisplayName("Should set and get id correctly")
    void testSetAndGetId() {
        // Arrange
        Long expectedId = 1L;

        // Act
        syncStateEntity.setId(expectedId);

        // Assert
        assertEquals(expectedId, syncStateEntity.getId());
    }

    @Test
    @DisplayName("Should set and get syncType correctly")
    void testSetAndGetSyncType() {
        // Arrange
        String expectedSyncType = "PRODUCT_SYNC";

        // Act
        syncStateEntity.setSyncType(expectedSyncType);

        // Assert
        assertEquals(expectedSyncType, syncStateEntity.getSyncType());
    }

    @Test
    @DisplayName("Should set and get enterpriseId correctly")
    void testSetAndGetEnterpriseId() {
        // Arrange
        String expectedEnterpriseId = "ENT-12345";

        // Act
        syncStateEntity.setEnterpriseId(expectedEnterpriseId);

        // Assert
        assertEquals(expectedEnterpriseId, syncStateEntity.getEnterpriseId());
    }

    @Test
    @DisplayName("Should set and get lastSyncDate correctly")
    void testSetAndGetLastSyncDate() {
        // Arrange
        Instant expectedLastSyncDate = Instant.now();

        // Act
        syncStateEntity.setLastSyncDate(expectedLastSyncDate);

        // Assert
        assertEquals(expectedLastSyncDate, syncStateEntity.getLastSyncDate());
    }

    @Test
    @DisplayName("Should set and get createdAt correctly")
    void testSetAndGetCreatedAt() {
        // Arrange
        Instant expectedCreatedAt = Instant.now();

        // Act
        syncStateEntity.setCreatedAt(expectedCreatedAt);

        // Assert
        assertEquals(expectedCreatedAt, syncStateEntity.getCreatedAt());
    }

    @Test
    @DisplayName("Should set and get updatedAt correctly")
    void testSetAndGetUpdatedAt() {
        // Arrange
        Instant expectedUpdatedAt = Instant.now();

        // Act
        syncStateEntity.setUpdatedAt(expectedUpdatedAt);

        // Assert
        assertEquals(expectedUpdatedAt, syncStateEntity.getUpdatedAt());
    }

    @Test
    @DisplayName("Should set and get tenantId correctly")
    void testSetAndGetTenantId() {
        // Arrange
        String expectedTenantId = "TENANT-001";

        // Act
        syncStateEntity.setTenantId(expectedTenantId);

        // Assert
        assertEquals(expectedTenantId, syncStateEntity.getTenantId());
    }

    @Test
    @DisplayName("Should set createdAt and updatedAt on onCreate")
    void testOnCreate_SetsTimestamps() throws Exception {
        // Arrange
        Instant beforeCreate = Instant.now();
        Method onCreateMethod = SyncStateEntity.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);

        // Act
        onCreateMethod.invoke(syncStateEntity);
        Instant afterCreate = Instant.now();

        // Assert
        assertNotNull(syncStateEntity.getCreatedAt());
        assertNotNull(syncStateEntity.getUpdatedAt());
        assertTrue(syncStateEntity.getCreatedAt().isAfter(beforeCreate.minusSeconds(1)));
        assertTrue(syncStateEntity.getCreatedAt().isBefore(afterCreate.plusSeconds(1)));
        // Check that createdAt and updatedAt are very close (within 1 second)
        assertTrue(Math.abs(syncStateEntity.getCreatedAt().toEpochMilli() - 
                           syncStateEntity.getUpdatedAt().toEpochMilli()) < 1000);
    }

    @Test
    @DisplayName("Should update only updatedAt on onUpdate")
    void testOnUpdate_UpdatesOnlyUpdatedAt() throws Exception {
        // Arrange
        Method onCreateMethod = SyncStateEntity.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);
        Method onUpdateMethod = SyncStateEntity.class.getDeclaredMethod("onUpdate");
        onUpdateMethod.setAccessible(true);
        
        onCreateMethod.invoke(syncStateEntity);
        Instant originalCreatedAt = syncStateEntity.getCreatedAt();
        Instant originalUpdatedAt = syncStateEntity.getUpdatedAt();
        
        // Wait a bit to ensure timestamps are different
        Thread.sleep(10);

        // Act
        onUpdateMethod.invoke(syncStateEntity);

        // Assert
        assertEquals(originalCreatedAt, syncStateEntity.getCreatedAt());
        assertNotEquals(originalUpdatedAt, syncStateEntity.getUpdatedAt());
        assertTrue(syncStateEntity.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    @DisplayName("Should handle multiple onCreate calls")
    void testOnCreate_MultipleCalls() throws Exception {
        // Arrange
        Method onCreateMethod = SyncStateEntity.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);
        
        // Act
        onCreateMethod.invoke(syncStateEntity);
        Instant firstCreatedAt = syncStateEntity.getCreatedAt();
        Instant firstUpdatedAt = syncStateEntity.getUpdatedAt();
        
        Thread.sleep(10);
        
        onCreateMethod.invoke(syncStateEntity);
        Instant secondCreatedAt = syncStateEntity.getCreatedAt();
        Instant secondUpdatedAt = syncStateEntity.getUpdatedAt();

        // Assert
        assertNotEquals(firstCreatedAt, secondCreatedAt);
        assertNotEquals(firstUpdatedAt, secondUpdatedAt);
        assertTrue(secondCreatedAt.isAfter(firstCreatedAt));
    }

    @Test
    @DisplayName("Should handle multiple onUpdate calls")
    void testOnUpdate_MultipleCalls() throws Exception {
        // Arrange
        Method onCreateMethod = SyncStateEntity.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);
        Method onUpdateMethod = SyncStateEntity.class.getDeclaredMethod("onUpdate");
        onUpdateMethod.setAccessible(true);
        
        onCreateMethod.invoke(syncStateEntity);
        Instant originalCreatedAt = syncStateEntity.getCreatedAt();
        
        Thread.sleep(10);
        onUpdateMethod.invoke(syncStateEntity);
        Instant firstUpdate = syncStateEntity.getUpdatedAt();
        
        Thread.sleep(10);

        // Act
        onUpdateMethod.invoke(syncStateEntity);
        Instant secondUpdate = syncStateEntity.getUpdatedAt();

        // Assert
        assertEquals(originalCreatedAt, syncStateEntity.getCreatedAt());
        assertTrue(secondUpdate.isAfter(firstUpdate));
    }

    @Test
    @DisplayName("Should maintain entity state across method calls")
    void testEntityState_PersistsAcrossMethodCalls() throws Exception {
        // Arrange
        Method onCreateMethod = SyncStateEntity.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);
        
        // Act
        syncStateEntity.setId(100L);
        syncStateEntity.setSyncType("INVENTORY_SYNC");
        syncStateEntity.setEnterpriseId("ENT-999");
        syncStateEntity.setLastSyncDate(Instant.now());
        syncStateEntity.setTenantId("TENANT-100");
        onCreateMethod.invoke(syncStateEntity);

        // Assert
        assertEquals(100L, syncStateEntity.getId());
        assertEquals("INVENTORY_SYNC", syncStateEntity.getSyncType());
        assertEquals("ENT-999", syncStateEntity.getEnterpriseId());
        assertNotNull(syncStateEntity.getLastSyncDate());
        assertEquals("TENANT-100", syncStateEntity.getTenantId());
        assertNotNull(syncStateEntity.getCreatedAt());
        assertNotNull(syncStateEntity.getUpdatedAt());
    }

    @Test
    @DisplayName("Should allow null values for optional fields")
    void testNullValues_ForOptionalFields() {
        // Act
        syncStateEntity.setId(null);
        syncStateEntity.setTenantId(null);

        // Assert
        assertNull(syncStateEntity.getId());
        assertNull(syncStateEntity.getTenantId());
    }

    @Test
    @DisplayName("Should handle edge case with minimum Long value")
    void testSetId_MinimumLongValue() {
        // Arrange
        Long minLongValue = Long.MIN_VALUE;

        // Act
        syncStateEntity.setId(minLongValue);

        // Assert
        assertEquals(minLongValue, syncStateEntity.getId());
    }

    @Test
    @DisplayName("Should handle edge case with maximum Long value")
    void testSetId_MaximumLongValue() {
        // Arrange
        Long maxLongValue = Long.MAX_VALUE;

        // Act
        syncStateEntity.setId(maxLongValue);

        // Assert
        assertEquals(maxLongValue, syncStateEntity.getId());
    }

    @Test
    @DisplayName("Should handle empty strings correctly")
    void testSetStrings_EmptyValues() {
        // Act
        syncStateEntity.setSyncType("");
        syncStateEntity.setEnterpriseId("");
        syncStateEntity.setTenantId("");

        // Assert
        assertEquals("", syncStateEntity.getSyncType());
        assertEquals("", syncStateEntity.getEnterpriseId());
        assertEquals("", syncStateEntity.getTenantId());
    }

    @Test
    @DisplayName("Should handle very long string values")
    void testSetStrings_VeryLongValues() {
        // Arrange
        String longString = "A".repeat(1000);

        // Act
        syncStateEntity.setSyncType(longString);
        syncStateEntity.setEnterpriseId(longString);
        syncStateEntity.setTenantId(longString);

        // Assert
        assertEquals(longString, syncStateEntity.getSyncType());
        assertEquals(longString, syncStateEntity.getEnterpriseId());
        assertEquals(longString, syncStateEntity.getTenantId());
    }

    @Test
    @DisplayName("Should handle past Instant values")
    void testSetLastSyncDate_PastDate() {
        // Arrange
        Instant pastDate = Instant.parse("2020-01-01T00:00:00Z");

        // Act
        syncStateEntity.setLastSyncDate(pastDate);

        // Assert
        assertEquals(pastDate, syncStateEntity.getLastSyncDate());
        assertTrue(syncStateEntity.getLastSyncDate().isBefore(Instant.now()));
    }

    @Test
    @DisplayName("Should handle future Instant values")
    void testSetLastSyncDate_FutureDate() {
        // Arrange
        Instant futureDate = Instant.parse("2030-12-31T23:59:59Z");

        // Act
        syncStateEntity.setLastSyncDate(futureDate);

        // Assert
        assertEquals(futureDate, syncStateEntity.getLastSyncDate());
        assertTrue(syncStateEntity.getLastSyncDate().isAfter(Instant.now()));
    }
}
