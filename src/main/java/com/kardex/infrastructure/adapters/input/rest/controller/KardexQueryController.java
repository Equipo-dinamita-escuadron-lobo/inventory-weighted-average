package com.kardex.infrastructure.adapters.input.rest.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kardex.application.ports.input.IKardexQueryPort;
import com.kardex.domain.model.Kardex;
import com.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import com.kardex.infrastructure.adapters.input.rest.dto.response.KardexDtoResponse;
import com.kardex.infrastructure.adapters.input.rest.mapper.IKardexResponseMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kardex/weighted-average")
public class KardexQueryController {

    private final IKardexQueryPort kardexQueryPort;
    private final IKardexResponseMapper kardexResponseMapper;

    @GetMapping("/kardex-by-product")
    public ResponseDto<Page<KardexDtoResponse>> getKardexByProductId(@RequestParam Long productId, Pageable pageable) {
        Page<Kardex> kardexPage = kardexQueryPort.findProductId(productId, pageable);
        Page<KardexDtoResponse> kardexDtoResponses = kardexPage.map(kardexResponseMapper::toDtoResponse);
        return ResponseDto.<Page<KardexDtoResponse>>builder()
                .data(kardexDtoResponses)
                .status(200)
                .message("Kardex records retrieved successfully").build();
    }
}
