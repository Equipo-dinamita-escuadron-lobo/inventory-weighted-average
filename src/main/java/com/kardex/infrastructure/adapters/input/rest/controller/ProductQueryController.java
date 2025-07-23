package com.kardex.infrastructure.adapters.input.rest.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kardex.application.ports.input.IProductQueryPort;
import com.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import com.kardex.infrastructure.adapters.input.rest.dto.response.ProductDtoResponse;
import com.kardex.infrastructure.adapters.input.rest.mapper.IProductResponseMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kardex/weighted-average")
public class ProductQueryController {
    private final IProductResponseMapper productResponseMapper;
    private final IProductQueryPort productQueryPort;

    @GetMapping("/products/{enterpriseId}")
    public ResponseDto<List<ProductDtoResponse>> getAllProducts(@PathVariable String enterpriseId) {
        List<ProductDtoResponse> productDtoResponses = productQueryPort.findAll(enterpriseId)
                .stream().map(productResponseMapper::toDtoResponse).toList();
        return ResponseDto.<List<ProductDtoResponse>>builder()
                .data(productDtoResponses)
                .status(200)
                .message("Products retrieved successfully").build();
    }
}
