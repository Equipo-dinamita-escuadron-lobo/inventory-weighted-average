package com.kardex.infrastructure.adapters.input.rest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import com.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;

import com.kardex.application.ports.input.IProductSyncCommandPort;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kardex/weighted-average/sync")
public class ProductSyncController {
    private final IProductSyncCommandPort productCommandPort;

    @GetMapping("/products/{enterpriseId}")
    public ResponseEntity<ResponseDto<String>> syncProducts(@PathVariable String enterpriseId) {
        String result = productCommandPort.syncProductsByEnterpriseId(enterpriseId);
        ResponseDto<String> responseDto = ResponseDto.<String>builder()
                .data(result)
                .status(200)
                .message("Synchronization completed").build();
        return ResponseEntity.ok(responseDto);
    }
}
