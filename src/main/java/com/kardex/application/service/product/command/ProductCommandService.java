package com.kardex.application.service.product.command;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kardex.application.ports.input.IProductSyncCommandPort;
import com.kardex.domain.model.Product;
import com.kardex.domain.port.IFormatterResultOutputPort;
import com.kardex.domain.port.IProductClientPort;
import com.kardex.domain.port.IProductCommandRepositoryPort;
import com.kardex.domain.port.ISyncStateRepositoryPort;
import com.kardex.domain.port.IMessageServicePort;
import com.kardex.domain.model.SyncState;
import com.kardex.infrastructure.adapters.config.i18n.MessageKeys;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Service for synchronizing products from external sources
 * 
 * Manages incremental product synchronization using timestamp-based
 * tracking to maintain data consistency across systems.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductCommandService implements IProductSyncCommandPort {

    private final IProductCommandRepositoryPort productCommandRepositoryPort;
    private final IProductClientPort productClient;
    private final ISyncStateRepositoryPort syncStateRepository;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IMessageServicePort messageService;

    private static final String SYNC_TYPE_PRODUCTS = "products";

    /**
     * @brief Synchronizes products for an enterprise from external API
     * @param enterpriseId Enterprise identifier to sync products for
     * @return Status message with sync results
     */
    @Override
    public String syncProductsByEnterpriseId(String enterpriseId) { 
        // 1. Obtain the last synchronization date
        Optional<Instant> lastSync = syncStateRepository.findLastSyncFor(SYNC_TYPE_PRODUCTS, enterpriseId);

        if (lastSync.isEmpty()) {
            // If no previous sync exists, create a new sync state with the current time
            Instant oldDate = Instant.parse("2000-01-01T00:00:00Z");
            lastSync = createSyncStateIfNotExists(enterpriseId, oldDate); 
            log.info(messageService.getMessage(MessageKeys.ERROR_MISSING_RECORD, enterpriseId));
        }

        log.info(messageService.getMessage(MessageKeys.LOG_OPERATION_STARTED, "syncProductsByEnterpriseId", enterpriseId, lastSync.get()));

        // 2. Moment before the call (this will be the new date if all goes well)
        Instant syncStartedAt = Instant.now();

        try {
            // 3. Call the API
            List<Product> updatedProducts = productClient
                .findAllProductsByEnterpriseId(enterpriseId, lastSync.get());

            // 4. Process the received products
            processUpdatedProducts(updatedProducts, enterpriseId);

            // 5. Only if everything was successful, update the date
            updateSyncState(enterpriseId, syncStartedAt);

            log.info(messageService.getMessage(MessageKeys.LOG_OPERATION_COMPLETED, enterpriseId, updatedProducts.size()));

            return "Successful synchronization: " + updatedProducts.size() + " products processed.";

        } catch (Exception e) {
            log.error(messageService.getMessage(MessageKeys.LOG_OPERATION_ERROR, enterpriseId), e);
            formatterResultOutputPort.returnBusinessRuleErrorResponse(500, 
                messageService.getMessage(MessageKeys.LOG_SYNC, e.getMessage()));
            throw e;
        }
    }

    private void processUpdatedProducts(List<Product> products, String enterpriseId) {
        try {
            if (products == null || products.isEmpty()) {
                log.info(messageService.getMessage(MessageKeys.LOG_OPERATION_ERROR, "processUpdatedProducts", "Enterprise ID: " + enterpriseId));
                return;
            }
            
            List<Product> productList = getProductsFromDto(products, enterpriseId);

            // Save all products to the database
            productCommandRepositoryPort.saveAll(productList);
            log.info(messageService.getMessage(MessageKeys.LOG_OPERATION_COMPLETED, "processUpdatedProducts"));
        } catch (Exception e) {
            log.error(messageService.getMessage(MessageKeys.LOG_OPERATION_ERROR, "processUpdatedProducts", e.getMessage()), e);
        }
    }

    private List<Product> getProductsFromDto(List<Product> products, String enterpriseId) {
        return products.stream()
            .map(dto -> Product.builder()
                .productId(dto.getProductId())
                .reference(dto.getReference())
                .name(dto.getName())
                .presentation(dto.getPresentation())
                .enterpriseId(enterpriseId)
                .state(dto.isState())
                .build())
            .toList();
    }

    private void updateSyncState(String enterpriseId, Instant syncDate) {
        Optional<SyncState> existingState = syncStateRepository
            .findBySyncTypeAndEnterpriseId(SYNC_TYPE_PRODUCTS, enterpriseId);
        
        if (existingState.isPresent()) {
            // Update existing record
            SyncState state = existingState.get();
            state.setLastSyncDate(syncDate);
            syncStateRepository.save(state);
        } else {
            // Create new record
            SyncState newState = new SyncState();
            newState.setSyncType(SYNC_TYPE_PRODUCTS);
            newState.setEnterpriseId(enterpriseId);
            newState.setLastSyncDate(syncDate);
            syncStateRepository.save(newState);
        }
    }

     private Optional<Instant> createSyncStateIfNotExists(String enterpriseId, Instant syncDate) {
        SyncState newState = new SyncState();
        newState.setSyncType(SYNC_TYPE_PRODUCTS);
        newState.setEnterpriseId(enterpriseId);
        newState.setLastSyncDate(syncDate);
        syncStateRepository.save(newState);
        log.info(messageService.getMessage(MessageKeys.LOG_OPERATION_COMPLETED, 
            "Created initial sync state for enterpriseId=" + enterpriseId + " with date=" + syncDate));
        return Optional.of(syncDate);
    }
    
}
