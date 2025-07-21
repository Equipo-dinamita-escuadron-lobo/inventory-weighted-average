package com.kardex.infrastructure.adapters.input.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kardex.application.ports.input.IKardexCommandPort;
import com.kardex.domain.model.Kardex;
import com.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import com.kardex.infrastructure.adapters.input.rest.dto.request.KardexPurchaseDtoRequest;
import com.kardex.infrastructure.adapters.input.rest.dto.response.KardexDtoResponse;
import com.kardex.infrastructure.adapters.input.rest.mapper.IKardexResponseMapper;
import com.kardex.infrastructure.adapters.input.rest.mapper.IKardexRestMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kardex/weighted-average")
public class KardexCommandController {

    private final IKardexCommandPort kardexCommandPort;
    private final IKardexRestMapper kardexRestMapper;
    private final IKardexResponseMapper kardexResponseMapper;

    @GetMapping
    public ResponseEntity<?> test(){
        kardexCommandPort.test("Hola");
        return ResponseEntity.ok("Hola");
    }

    @PostMapping("/purchase")
    public ResponseEntity<ResponseDto<KardexDtoResponse>> purchaseKardex(@RequestBody KardexPurchaseDtoRequest kardexPurchaseDtoRequest) {
        Kardex response = kardexCommandPort.registerPurchase(kardexRestMapper.toDomain(kardexPurchaseDtoRequest));
        KardexDtoResponse kardexDtoResponse = kardexResponseMapper.toDtoResponse(response);
        ResponseDto<KardexDtoResponse> responseDto = ResponseDto.<KardexDtoResponse>builder()
                .data(kardexDtoResponse)
                .status(200)
                .message("Kardex purchase registered successfully").build();
        return responseDto.of();
    }

    @PostMapping("/sale")
    public ResponseEntity<ResponseDto<KardexDtoResponse>> saleKardex(@RequestBody KardexPurchaseDtoRequest kardexPurchaseDtoRequest) {
        Kardex response = kardexCommandPort.registerSale(kardexRestMapper.toDomain(kardexPurchaseDtoRequest));
        KardexDtoResponse kardexDtoResponse = kardexResponseMapper.toDtoResponse(response);
        ResponseDto<KardexDtoResponse> responseDto = ResponseDto.<KardexDtoResponse>builder()
                .data(kardexDtoResponse)
                .status(200)
                .message("Kardex sale registered successfully").build();
        return responseDto.of();    
    }
    
}
