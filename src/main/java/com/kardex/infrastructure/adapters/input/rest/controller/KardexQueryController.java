package com.kardex.infrastructure.adapters.input.rest.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kardex.application.ports.input.IKardexQueryPort;
import com.kardex.domain.model.Kardex;
import com.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import com.kardex.infrastructure.adapters.input.rest.dto.request.KardexFilterDto;
import com.kardex.infrastructure.adapters.input.rest.dto.response.KardexDtoResponse;
import com.kardex.infrastructure.adapters.input.rest.mapper.IKardexResponseMapper;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

/**
 * @brief REST controller for Kardex inventory movement queries
 * 
 * Provides HTTP endpoints for retrieving inventory movement records
 * with pagination and optional date filtering capabilities.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kardex/weighted-average")
@Validated
public class KardexQueryController {

    private final IKardexQueryPort kardexQueryPort;
    private final IKardexResponseMapper kardexResponseMapper;

    /**
     * @brief Retrieves kardex records for a specific product with pagination
     * @param productId Product identifier to filter records
     * @param filter Optional date range filter
     * @param pageable Pagination parameters
     * @return Paginated response with kardex records
     */
    @GetMapping("/kardex-by-product")
    public ResponseDto<Page<KardexDtoResponse>> getKardexByProductId(
            @RequestParam @NotNull(message = "The product ID is required.") Long productId,
            @Valid @ModelAttribute KardexFilterDto filter,
            Pageable pageable) {

        Page<Kardex> kardexPage = kardexQueryPort.findProductId(productId, pageable, filter.getStartDate(), filter.getEndDate());
        Page<KardexDtoResponse> kardexDtoResponses = kardexPage.map(kardexResponseMapper::toDtoResponse);
        return ResponseDto.<Page<KardexDtoResponse>>builder()
                .data(kardexDtoResponses)
                .status(200)
                .message("Kardex records retrieved successfully").build();
    }
}
