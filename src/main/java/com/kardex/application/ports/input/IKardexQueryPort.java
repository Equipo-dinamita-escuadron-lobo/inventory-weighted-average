package com.kardex.application.ports.input;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kardex.domain.model.Kardex;

/**
 * @brief Input port for Kardex query operations
 * 
 * Provides query capabilities for retrieving inventory movement records
 * with optional date filtering and pagination support.
 */
public interface IKardexQueryPort {
    /**
     * @brief Finds kardex records for a specific product
     * @param productId Product identifier
     * @param pageable Pagination parameters
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return Paginated kardex records
     */
    Page<Kardex>  findProductId(Long productId, Pageable pageable, LocalDate startDate, LocalDate endDate);
}
