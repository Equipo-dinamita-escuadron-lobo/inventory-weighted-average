package com.kardex.infrastructure.adapters.input.rest.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kardex.application.ports.input.IKardexCommandPort;
import com.kardex.application.ports.input.IKardexQueryPort;
import com.kardex.domain.model.Kardex;
import com.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import com.kardex.infrastructure.adapters.input.rest.dto.request.KardexPurchaseDtoRequest;
import com.kardex.infrastructure.adapters.input.rest.dto.response.KardexDtoResponse;
import com.kardex.infrastructure.adapters.input.rest.mapper.IKardexRestMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kardex")
public class KardexController {

    private final IKardexCommandPort kardexCommandPort;
    private final IKardexQueryPort kardexQueryPort;
    private final IKardexRestMapper kardexRestMapper;

    @GetMapping
    public ResponseEntity<?> test(){
        kardexCommandPort.test("Hola");
        return ResponseEntity.ok("Hola");
    }

    @PostMapping("/purchase")
    public ResponseEntity<ResponseDto<KardexDtoResponse>> purchaseKardex(@RequestBody KardexPurchaseDtoRequest kardexPurchaseDtoRequest) {
        Kardex response = kardexCommandPort.registerPurchase(kardexRestMapper.toDomain(kardexPurchaseDtoRequest));
        KardexDtoResponse kardexDtoResponse = kardexRestMapper.toDtoResponse(response);
        ResponseDto<KardexDtoResponse> responseDto = ResponseDto.<KardexDtoResponse>builder()
                .data(kardexDtoResponse)
                .status(200)
                .message("Kardex purchase registered successfully").build();
        return responseDto.of();
    }

    @GetMapping("/product")
    public ResponseDto<Page<KardexDtoResponse>> getKardexByProductId(@RequestParam Long productId, Pageable pageable) {
        Page<Kardex> kardexPage = kardexQueryPort.findProductId(productId, pageable);
        Page<KardexDtoResponse> kardexDtoResponses = kardexPage.map(kardexRestMapper::toDtoResponse);
        return ResponseDto.<Page<KardexDtoResponse>>builder()
                .data(kardexDtoResponses)
                .status(200)
                .message("Kardex records retrieved successfully").build();
    }
    
}
