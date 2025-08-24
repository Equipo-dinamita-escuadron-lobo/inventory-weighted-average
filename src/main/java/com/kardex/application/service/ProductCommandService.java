package com.kardex.application.service;

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
import com.kardex.domain.model.SyncState;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductCommandService implements IProductSyncCommandPort {

    private final IProductCommandRepositoryPort productCommandRepositoryPort;
    private final IProductClientPort productClient;
    private final ISyncStateRepositoryPort syncStateRepository;
    private final IFormatterResultOutputPort formatterResultOutputPort;

    private static final String SYNC_TYPE_PRODUCTS = "products";

    @Override
    public String syncProductsByEnterpriseId(String enterpriseId) {
        // 1. Obtain the last synchronization date
        Optional<Instant> lastSync = syncStateRepository.findLastSyncFor(SYNC_TYPE_PRODUCTS, enterpriseId);

        if (lastSync.isEmpty()) {
            // If no previous sync exists, create a new sync state with the current time
            Instant oldDate = Instant.parse("2000-01-01T00:00:00Z");
            lastSync = createSyncStateIfNotExists(enterpriseId, oldDate); 
            log.info("No previous sync found for enterprise {}, creating new sync state.", enterpriseId);
        }

        log.info("Last synchronization date for company {}: {}", enterpriseId, lastSync.get());

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

            log.info("Successful synchronization for company {}: {} products processed",enterpriseId, updatedProducts.size());

            return "Successful synchronization: " + updatedProducts.size() + " products processed.";

        } catch (Exception e) {
            log.error("Error in product synchronization for company {}", enterpriseId, e);
            formatterResultOutputPort.returnBusinessRuleErrorResponse(500, "Error synchronizing products: " + e.getMessage());
            throw e;
        }
    }

    private void processUpdatedProducts(List<Product> products, String enterpriseId) {
        try {
            if (products == null || products.isEmpty()) {
                log.info("No products to process for enterprise {}", enterpriseId);
                return;
            }
            
            List<Product> productList = getProductsFromDto(products, enterpriseId);

            // Save all products to the database
            String result = productCommandRepositoryPort.saveAll(productList);
            log.info("Products saved: {}", result);
        } catch (Exception e) {
            log.error("Error processing products for enterprise {}", enterpriseId, e);
        }
    }

    private List<Product> getProductsFromDto(List<Product> products, String enterpriseId) {
        return products.stream()
            .map(dto -> Product.builder()
                .idProduct(dto.getIdProduct())
                .reference(dto.getReference())
                .name(dto.getName())
                .presentation(dto.getPresentation())
                .manager(null)
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
        log.info("Created new sync state for enterprise {}", enterpriseId);
        return Optional.of(syncDate);
    }
    
}
